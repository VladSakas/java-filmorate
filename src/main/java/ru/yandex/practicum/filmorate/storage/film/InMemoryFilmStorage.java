package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Фильм добавлен в хранилище: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        log.debug("Фильм обновлён: {}", film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void remove(Film film) {
        films.remove(film.getId());
        log.debug("Фильм удалён: {}", film);
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikes().add(userId);
            log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikes().remove(userId);
            log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        }
    }

    @Override
    public Collection<Film> getTopFilms(int count, Integer genreId, Integer year) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
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