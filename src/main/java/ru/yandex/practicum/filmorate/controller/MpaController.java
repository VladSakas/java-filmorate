package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final JdbcTemplate jdbc;

    @GetMapping
    public Collection<MpaRating> getAllMpa() {
        return jdbc.query("SELECT id, name FROM mpa_ratings ORDER BY id",
                (rs, rowNum) -> {
                    MpaRating mpa = new MpaRating();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                });
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable int id) {
        List<MpaRating> mpaList = jdbc.query("SELECT id, name FROM mpa_ratings WHERE id = ?",
                (rs, rowNum) -> {
                    MpaRating mpa = new MpaRating();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }, id);

        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return mpaList.get(0);
    }
}