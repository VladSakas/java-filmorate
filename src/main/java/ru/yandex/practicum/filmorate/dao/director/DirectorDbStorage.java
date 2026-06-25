package ru.yandex.practicum.filmorate.dao.director;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;

    private static final String UPDATE_QUERY =
            "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM directors WHERE id = ?";
    private static final String FIND_BY_ID_QUERY =
            "SELECT id, name FROM directors WHERE id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT id, name FROM directors ORDER BY id";

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", director.getName());
        director.setId(simpleJdbcInsert.executeAndReturnKey(params).longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        int updatedRows = jdbc.update(UPDATE_QUERY, director.getName(), director.getId());
        if (updatedRows == 0) {
            throw new NotFoundException("Режиссер не найден с id=" + director.getId());
        }
        return getDirectorById(director.getId()).orElseThrow();
    }

    @Override
    public void removeDirector(Long id) {
        jdbc.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<Director> getDirectorById(Long id) {
        try {
            Director director = jdbc.queryForObject(FIND_BY_ID_QUERY, (rs, rowNum) -> {
                Director d = new Director();
                d.setId(rs.getLong("id"));
                d.setName(rs.getString("name"));
                return d;
            }, id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Director> getAllDirectors() {
        return jdbc.query(FIND_ALL_QUERY, (rs, rowNum) -> {
            Director d = new Director();
            d.setId(rs.getLong("id"));
            d.setName(rs.getString("name"));
            return d;
        });
    }
}