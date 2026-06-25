package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    Director addDirector(Director director);

    Collection<Director> getAllDirectors();

    Optional<Director> getDirectorById(Long id);

    Director updateDirector(Director director);

    void removeDirector(Long id);

}
