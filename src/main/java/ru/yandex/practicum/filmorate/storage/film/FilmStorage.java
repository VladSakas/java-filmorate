package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void remove(Film film);

    Collection<Film> getAll();

    Optional<Film> getById(Long id);
}
