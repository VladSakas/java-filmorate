package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        log.info("Добавление режиссёра: name={}", director.getName());
        return directorStorage.addDirector(director);
    }

    public Collection<Director> getAllDirectors() {
        log.debug("Получение списка всех режиссёров");
        Collection<Director> directors = directorStorage.getAllDirectors();
        log.debug("Найдено режиссёров: {}", directors.size());
        return directors;
    }

    public Director getDirectorById(Long id) {
        log.debug("Получение режиссёра по id={}", id);
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public Director updateDirector(Director director) {
        log.info("Обновление режиссёра: id={}, name={}", director.getId(), director.getName());
        return directorStorage.updateDirector(director);
    }

    public void removeDirector(Long id) {
        log.info("Удаление режиссёра: id={}", id);
        directorStorage.removeDirector(id);
    }
}
