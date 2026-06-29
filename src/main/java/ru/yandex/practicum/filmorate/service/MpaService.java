package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;

    public Collection<MpaRating> getAllMpaRatings() {
        return mpaStorage.getAll();
    }

    public MpaRating getMpaRatingById(int id) {
        return mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA c id= " + id + " не найден"));
    }
}
