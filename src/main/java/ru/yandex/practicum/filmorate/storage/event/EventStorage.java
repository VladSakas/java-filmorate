package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {
    void saveEvent(Event event);

    List<Event> getEventByUserId(Long userId);
}