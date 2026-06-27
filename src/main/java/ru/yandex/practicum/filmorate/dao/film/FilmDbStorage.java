package ru.yandex.practicum.filmorate.dao.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Primary
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
                    "LEFT JOIN likes l ON f.id = l.film_id ";
    private static final String REMOVE_FILM_QUERY =
            "DELETE FROM films WHERE id = ?";
    private static final String DELETE_DIRECTORS_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String INSERT_DIRECTOR_QUERY =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String LOAD_DIRECTORS_QUERY =
            "SELECT d.id, d.name FROM film_directors fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id = ? ORDER BY d.id";
    private static final String FIND_BY_DIRECTOR_LIKES_QUERY = """
            SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            JOIN film_directors fd ON fd.film_id = f.id
            LEFT JOIN likes l ON f.id = l.film_id
            WHERE fd.director_id = ? GROUP BY f.id
            ORDER BY COUNT(l.user_id) DESC
            """;
    private static final String FIND_BY_DIRECTOR_YEAR_QUERY = """
            SELECT f.*, m.id as mpa_id, m.name as mpa_name FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            JOIN film_directors fd ON fd.film_id = f.id
            WHERE fd.director_id = ?
            ORDER BY f.release_date ASC
            """;
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
    private static final String FIND_RECOMMENDATIONS_QUERY = """
                SELECT f.*, m.id as mpa_id, m.name as mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                JOIN likes l ON f.id = l.film_id
                WHERE l.user_id = ?
                AND f.id NOT IN (SELECT film_id FROM likes WHERE user_id = ?)
            """;
    private static final String SEARCH_FILM_QUERY = """
            SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE %s
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
        updateDirectors(film);
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
        jdbc.update(DELETE_DIRECTORS_QUERY, film.getId());
        updateDirectors(film);
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
            loadDirectors(film);
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
                loadDirectors(film);
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

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public Collection<Film> getTopFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(GET_TOP_FILMS_QUERY);
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append(" LEFT JOIN film_genres fg ON f.id = fg.film_id ");
        }

        List<String> conditions = new ArrayList<>();

        if (year != null) {
            conditions.add(" YEAR(f.release_date) = ? ");
            params.add(year);
        }

        if (genreId != null) {
            conditions.add(" fg.genre_id = ? ");
            params.add(genreId);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        sql.append(" GROUP BY f.id, m.id, m.name ORDER BY COUNT(l.user_id) DESC LIMIT ?");
        params.add(count);

        List<Film> films = jdbc.query(sql.toString(), this::mapRowToFilm, params.toArray());
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        List<Film> films;
        if ("likes".equals(sortBy)) {
            films = jdbc.query(FIND_BY_DIRECTOR_LIKES_QUERY, this::mapRowToFilm, directorId);
        } else if ("year".equals(sortBy)) {
            films = jdbc.query(FIND_BY_DIRECTOR_YEAR_QUERY, this::mapRowToFilm, directorId);
        } else {
            throw new NotFoundException("Unknown sortBy: " + sortBy);
        }

        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
            loadDirectors(film);
        }
        return films;
    }

    private void loadDirectors(Film film) {
        List<Director> directors = jdbc.query(LOAD_DIRECTORS_QUERY, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        }, film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
    }

    private void updateDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbc.batchUpdate(INSERT_DIRECTOR_QUERY, directors, directors.size(), (ps, director) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, director.getId());
                }
        );
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbc.query(GET_COMMON_FILMS_QUERY, this::mapRowToFilm, userId, friendId);
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public List<Film> findRecommendationsForUser(Long userId, Long matchUserId) {
        List<Film> films = jdbc.query(FIND_RECOMMENDATIONS_QUERY, this::mapRowToFilm, matchUserId, userId);
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public void delete(Long id) {
        String sql = REMOVE_FILM_QUERY;
        jdbc.update(sql, id);
    }

    @Override
    public List<Film> searchFilms(String query, List<String> by) {
        boolean byTitle = by.contains("title");
        boolean byDirector = by.contains("director");

        String searchQuery = "%" + query.toLowerCase() + "%";

        List<Object> params = new ArrayList<>();
        params.add(searchQuery);
        if (byTitle && byDirector) {
            params.add(searchQuery);
        }

        String sql = String.format(SEARCH_FILM_QUERY, buildSearchCondition(byTitle, byDirector));

        List<Film> films = jdbc.query(sql, this::mapRowToFilm, params.toArray());
        for (Film film : films) {
            loadGenres(film);
            loadDirectors(film);
        }

        return films;
    }

    private String buildSearchCondition(boolean byTitle, boolean byDirector) {
        if (byTitle && byDirector) {
            return "LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?";
        } else if (byTitle) {
            return "LOWER(f.name) LIKE ?";
        } else {
            return "LOWER(d.name) LIKE ?";
        }
    }
}