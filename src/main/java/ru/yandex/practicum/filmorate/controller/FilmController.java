package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film add(@RequestBody Film film) {
        log.info("Добавление фильма: {}", film);

        try {
            FilmValidator.validate(film);
        } catch (ValidationException e) {
            log.error("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм успешно добавлен: {}, id={}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Обновление фильма: {}", film);

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        try {
            FilmValidator.validate(film);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении фильма id={}: {}", film.getId(), e.getMessage());
            throw e;
        }

        films.put(film.getId(), film);

        log.info("Фильм успешно обновлён: {}, id={}", film.getName(), film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.debug("Запрос всех фильмов, найдено: {} фильмов", films.size());
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
