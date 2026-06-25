package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void remove(Film film);

    Collection<Film> getAll();

    Optional<Film> getById(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Film> getTopFilms(int count, Integer genreId,Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> findRecommendationsForUser(Long userId, Long matchUserId);
}
