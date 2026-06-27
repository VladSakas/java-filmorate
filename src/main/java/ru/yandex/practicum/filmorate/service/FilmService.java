package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private static final String CHECK_MPA_EXISTS_QUERY =
            "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
    private static final String CHECK_GENRE_EXISTS_QUERY =
            "SELECT COUNT(*) FROM genres WHERE id = ?";

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
                    CHECK_MPA_EXISTS_QUERY,
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
                        CHECK_GENRE_EXISTS_QUERY,
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
                    CHECK_MPA_EXISTS_QUERY,
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
                        CHECK_GENRE_EXISTS_QUERY,
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

    public Collection<Film> getTopFilms(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            log.warn("Запрос популярных фильмов: count={} не положительное число", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        return filmStorage.getTopFilms(count, genreId, year);
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссёра: directorId={}, sortBy={}", directorId, sortBy);
        if (!"likes".equals(sortBy) && !"year".equals(sortBy)) {
            log.warn("Некорректный sortBy: {}", sortBy);
        }
        Collection<Film> films = filmStorage.getFilmsByDirector(directorId, sortBy);
        log.info("Найдено фильмов режиссёра {}: {}", directorId, films.size());
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Запрос общих фильмов пользователей id={} и id={}", userId, friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        log.info("Поиск фильма запрос:{}, фильтры: {}", query, by);
        return filmStorage.searchFilms(query, by);
    }
}