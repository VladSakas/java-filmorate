package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    private void validateFilmExists(Long filmId) {
        if (filmStorage.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}