package ru.yandex.practicum.filmorate.dao.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    private static final String ADD_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String LOAD_GENRES_QUERY =
            "SELECT g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ? ORDER BY g.id";
    private static final String LOAD_LIKES_QUERY =
            "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_TOP_FILMS_QUERY =
            "SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
    private static final String REMOVE_FILM_QUERY =
            "DELETE FROM films WHERE id = ?";
    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count
                    FROM films f
                    LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                    JOIN likes l1 ON f.id = l1.film_id AND l1.user_id = ?
                    JOIN likes l2 ON f.id = l2.film_id AND l2.user_id = ?
                    LEFT JOIN likes l ON f.id = l.film_id
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
            ORDER BY likes_count DESC
            """;

    @Override
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(ADD_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        updateGenres(film);
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        jdbc.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        updateGenres(film);
        return getById(film.getId()).orElseThrow();
    }

    private void updateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_GENRE_QUERY, film.getId(), genre.getId());
        }
    }

    @Override
    public void remove(Film film) {
        jdbc.update(REMOVE_FILM_QUERY, film.getId());
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, this::mapRowToFilm);
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public Optional<Film> getById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, this::mapRowToFilm, id);
            if (film != null) {
                loadGenres(film);
                loadLikes(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        if (rs.getObject("mpa_id") != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(LOAD_GENRES_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadLikes(Film film) {
        List<Long> likes = jdbc.queryForList(LOAD_LIKES_QUERY, Long.class, film.getId());
        film.getLikes().clear();
        film.getLikes().addAll(likes);
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public Collection<Film> getTopFilms(int count) {
        List<Film> films = jdbc.query(GET_TOP_FILMS_QUERY, this::mapRowToFilm, count);
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbc.query(GET_COMMON_FILMS_QUERY, this::mapRowToFilm, userId, friendId);
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbc.update(sql, id);
    }
}