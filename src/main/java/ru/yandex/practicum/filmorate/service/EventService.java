package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MissingUserIdException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public void createEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        Event event = Event.builder()
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .timestamp(System.currentTimeMillis())
                .build();
        eventStorage.saveEvent(event);
        log.info("Создано событие: user={}, type={}, op={}, entity={}", userId, eventType, operation, entityId);
    }

    public List<Event> getFeed(Long userId) {
        log.info("Запрос ленты для пользователя: {}", userId);

        if (userId == null) {
            log.error("Попытка получить feed без ID пользователя");
            throw new MissingUserIdException("ID пользователя обязателен для получения ленты событий");
        }

        if (userStorage.getById(userId).isEmpty()) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Event> events = eventStorage.getEventByUserId(userId);
        log.info("Найдено событий: {}", events.size());

        return events;
    }
}