package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public User add(User user) {
        log.info("Создание пользователя: {}", user);
        UserValidator.normalizeName(user);
        UserValidator.validate(user);
        User savedUser = userStorage.add(user);
        log.info("Пользователь успешно создан: {}", savedUser);
        return savedUser;
    }

    public User update(User user) {
        log.info("Обновление данных пользователя: {}", user);

        if (user.getId() == null) {
            log.warn("При обновлении данных пользователя не указан id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (userStorage.getById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        UserValidator.normalizeName(user);
        UserValidator.validate(user);
        User updatedUser = userStorage.update(user);
        log.info("Данные пользователя успешно обновлены: {}", updatedUser);
        return updatedUser;
    }

    public Collection<User> getAll() {
        Collection<User> users = userStorage.getAll();
        log.debug("Запрос всех пользователей, найдено: {}", users.size());
        return users;
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
        if (userStorage.getById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь " + friendId + " не найден");
        }
        userStorage.addFriend(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
        if (userStorage.getById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь " + friendId + " не найден");
        }
        userStorage.removeFriend(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
        if (userStorage.getById(otherId).isEmpty()) {
            throw new NotFoundException("Пользователь " + otherId + " не найден");
        }
        return userStorage.getCommonFriends(userId, otherId);
    }

    public void delete(Long id) {
        log.info("Удаление пользователя с id {}", id);
        userStorage.getById(id);
        userStorage.delete(id);
    }
}