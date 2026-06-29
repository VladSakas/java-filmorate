package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(@Qualifier("userDbStorage") UserStorage userStorage,
                                 @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getRecommendations(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Optional<Long> matchUserId = userStorage.findBestMatchUserId(userId);
        if (matchUserId.isEmpty()) {
            log.info("Для пользователя {} не найдено похожих пользователей", userId);
            return Collections.emptyList();
        }

        log.info("Найден похожий пользователь: {}", matchUserId.get());
        return filmStorage.findRecommendationsForUser(userId, matchUserId.get());
    }
}