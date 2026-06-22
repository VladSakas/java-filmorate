package ru.yandex.practicum.filmorate.dao.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM genres ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM genres WHERE id = ?";

    @Override
    public Collection<Genre> getAll() {
        return jdbc.query(FIND_ALL_QUERY,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                });
    }

    @Override
    public Optional<Genre> getById(int id) {
        return jdbc.query(FIND_BY_ID_QUERY,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }, id).stream().findFirst();
    }
}