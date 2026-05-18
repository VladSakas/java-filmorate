package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(final FilmStorage filmStorage, final UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(Film film) {
        log.info("Добавление фильма: {}", film);
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

        FilmValidator.validate(film);
        Film updatedFilm = filmStorage.update(film);

        log.info("Фильм успешно обновлён: {}", updatedFilm);
        return updatedFilm;
    }

    public Collection<Film> getAll() {
        Collection<Film> films = filmStorage.getAll();
        log.debug("Запрос всех фильмов, найдено: {} фильмов", films.size());
        return films;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        validateUserExists(userId);

        if (film.getLikes().contains(userId)) {
            throw new IllegalArgumentException("Фильм можно лайкнуть только один раз!");
        }

        film.getLikes().add(userId);
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        validateUserExists(userId);
        film.getLikes().remove(userId);
        log.info("Пользователю {} больше не нравится фильм {}", userId, filmId);
    }

    public Collection<Film> getTopFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя не существует"));
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }
}
