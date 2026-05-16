package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Создание пользователя: {}", user);

        UserValidator.normalizeName(user);
        UserValidator.validate(user);

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Пользователь успешно создан: {}", user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Обновление данных пользователя: {}", user);

        if (user.getId() == null) {
            log.warn("При обновлении данных пользователя не указан id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        UserValidator.normalizeName(user);
        UserValidator.validate(user);

        users.put(user.getId(), user);
        log.info("Данные пользователя успешно обновлены: {}", user);

        return user;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.debug("Запрос всех пользователей, найдено: {}", users.size());
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}