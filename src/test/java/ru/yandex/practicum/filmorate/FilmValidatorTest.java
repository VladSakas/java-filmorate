package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static ru.yandex.practicum.filmorate.validator.FilmValidator.MAX_DESCRIPTION_LENGTH;
import static ru.yandex.practicum.filmorate.validator.FilmValidator.MIN_RELEASE_DATE;

public class FilmValidatorTest {

    @Test
    public void testEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        try {
            FilmValidator.validate(film);
            fail("Название не может быть пустым");
        } catch (ValidationException e) {
            assertEquals("Название фильма не может быть пустым", e.getMessage());
        }
    }

    @Test
    public void testDescriptionTooLong() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        try {
            FilmValidator.validate(film);
            fail("Описание не может быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        } catch (ValidationException e) {
            assertEquals("Максимальная длина описания - " +
                    MAX_DESCRIPTION_LENGTH + " символов", e.getMessage());
        }
    }

    @Test
    public void testReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        try {
            FilmValidator.validate(film);
            fail("Дата релиза не может быть раньше 28 декабря 1895 года");
        } catch (ValidationException e) {
            assertEquals("Дата релиза не может быть раньше " + MIN_RELEASE_DATE, e.getMessage());
        }
    }

    @Test
    public void testDurationZero() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        try {
            FilmValidator.validate(film);
            fail("Продолжительность должна быть положительной");
        } catch (ValidationException e) {
            assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
        }
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        try {
            FilmValidator.validate(film);
        } catch (ValidationException e) {
            fail("Валидный фильм не должен вызывать исключение");
        }
    }
}