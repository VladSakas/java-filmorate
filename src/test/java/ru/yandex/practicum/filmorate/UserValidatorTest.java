package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class UserValidatorTest {

    @Test
    void testEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 5, 29));

        try {
            UserValidator.validate(user);
            fail("Email не может быть пустым");
        } catch (ValidationException e) {
            assertEquals("Email должен быть указан", e.getMessage());
        }
    }

    @Test
    void testEmailWithoutAt() {
        User user = new User();
        user.setEmail("usermail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 5, 29));

        try {
            UserValidator.validate(user);
            fail("Email должен содержать @");
        } catch (ValidationException e) {
            assertEquals("Email должен содержать @", e.getMessage());
        }
    }

    @Test
    void testEmptyLogin() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1993, 5, 29));

        try {
            UserValidator.validate(user);
            fail("Логин не может быть пустым");
        } catch (ValidationException e) {
            assertEquals("Логин должен быть указан", e.getMessage());
        }
    }

    @Test
    void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("user name");
        user.setBirthday(LocalDate.of(1993, 5, 29));

        try {
            UserValidator.validate(user);
            fail("Логин не должен содержать пробелы");
        } catch (ValidationException e) {
            assertEquals("Логин не должен содержать пробелы", e.getMessage());
        }
    }

    @Test
    void testBirthdayInFuture() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        try {
            UserValidator.validate(user);
            fail("Дата рождения не может быть в будущем");
        } catch (ValidationException e) {
            assertEquals("Дата рождения не может быть в будущем", e.getMessage());
        }
    }

    @Test
    void testValidUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 5, 29));

        try {
            UserValidator.validate(user);
        } catch (ValidationException e) {
            fail("Валидный пользователь не должен вызывать исключение");
        }
    }
}