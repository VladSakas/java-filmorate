package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film add(Film film) {
        log.info("Добавление фильма: {}", film);

        if (film.getMpa() != null && film.getMpa().getId() > 0) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?",
                    Integer.class,
                    film.getMpa().getId()
            );
            if (count == null || count == 0) {
                throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genres WHERE id = ?",
                        Integer.class,
                        genre.getId()
                );
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        FilmValidator.validate(film);
        Film savedFilm = filmStorage.add(film);
        log.info("Фильм успешно добавлен: {}", savedFilm);
        return savedFilm;
    }

    public Film update(Film film) {
        log.info("Обновление фильма: {}", film);

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        if (film.getMpa() != null && film.getMpa().getId() > 0) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?",
                    Integer.class,
                    film.getMpa().getId()
            );
            if (count == null || count == 0) {
                throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genres WHERE id = ?",
                        Integer.class,
                        genre.getId()
                );
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        FilmValidator.validate(film);
        Film updatedFilm = filmStorage.update(film);

        log.info("Фильм успешно обновлён: {}", updatedFilm);
        return updatedFilm;
    }

    public Film getById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public Collection<Film> getAll() {
        Collection<Film> films = filmStorage.getAll();
        log.debug("Запрос всех фильмов, найдено: {} фильмов", films.size());
        return films;
    }

    public void addLike(Long filmId, Long userId) {
        if (filmStorage.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public Collection<Film> getTopFilms(int count) {
        return filmStorage.getTopFilms(count);
    }
}