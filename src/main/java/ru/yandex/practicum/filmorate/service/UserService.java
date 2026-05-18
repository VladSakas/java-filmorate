package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(@RequestBody User user) {
        log.info("Создание пользователя: {}", user);

        UserValidator.normalizeName(user);
        UserValidator.validate(user);
        User savedUser = userStorage.add(user);

        log.info("Пользователь успешно создан: {}", savedUser);

        return savedUser;
    }

    public User update(@RequestBody User user) {
        log.info("Обновление данных пользователя: {}", user);

        if (user.getId() == null) {
            log.warn("При обновлении данных пользователя не указан id");
            throw new ConditionsNotMetException("Id должен быть указан");
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

    public void addFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.debug("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.debug("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = getUserOrThrow(userId);

        Collection<User> friends = new ArrayList<>();
        for (Long friendsId : user.getFriends()) {
            friends.add(getUserOrThrow(friendsId));
        }
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        Collection<User> commonFriends = new ArrayList<>();

        for (Long friendsId : user.getFriends()) {
            if (friend.getFriends().contains(friendsId)) {
                commonFriends.add(getUserOrThrow(friendsId));
            }
        }
        return commonFriends;
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя не существует"));
    }
}