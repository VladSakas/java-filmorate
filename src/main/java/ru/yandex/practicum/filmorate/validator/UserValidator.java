package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
@Component
public class UserValidator {
    public static void validate(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Email не введён");
            throw new ValidationException("Email должен быть указан");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Введённый Email не содержит символ '@'");
            throw new ValidationException("Email должен содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Логин не введён");
            throw new ValidationException("Логин должен быть указан");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Логин не должен содержать пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

    }

    public static void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пустое имя пользователя заменено на логин: {}", user.getLogin());
        }
    }

}