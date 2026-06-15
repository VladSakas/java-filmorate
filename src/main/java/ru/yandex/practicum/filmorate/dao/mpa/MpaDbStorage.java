package ru.yandex.practicum.filmorate.dao.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM mpa_ratings WHERE id = ?";

    @Override
    public Collection<MpaRating> getAll() {
        return jdbc.query(FIND_ALL_QUERY,
                (rs, rowNum) -> {
                    MpaRating mpa = new MpaRating();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                });
    }

    @Override
    public Optional<MpaRating> getById(int id) {
        return jdbc.query(FIND_BY_ID_QUERY,
                (rs, rowNum) -> {
                    MpaRating mpa = new MpaRating();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }, id).stream().findFirst();
    }
}
