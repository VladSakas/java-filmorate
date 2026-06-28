package ru.yandex.practicum.filmorate.dao.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbc;

    private static final String SAVE_EVENT_QUERY =
            "INSERT INTO user_events (user_id, event_type, operation, entity_id, timestamp) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_EVENTS_BY_USER_QUERY =
            "SELECT * FROM user_events WHERE user_id = ? ORDER BY timestamp ASC";


    @Override
    public void saveEvent(Event event) {
        log.info("Сохранение события: userId={}, type={}, op={}, entityId={}",
                event.getUserId(), event.getEventType(), event.getOperation(), event.getEntityId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SAVE_EVENT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, event.getUserId());
            ps.setString(2, event.getEventType().name());
            ps.setString(3, event.getOperation().name());
            ps.setLong(4, event.getEntityId());
            ps.setLong(5, event.getTimestamp());
            return ps;
        }, keyHolder);
        event.setEventId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Событие сохранено с id: {}", event.getEventId());
    }

    @Override
    public List<Event> getEventByUserId(Long userId) {
        log.info("Запрос событий из БД для userId={}", userId);
        List<Event> events = jdbc.query(GET_EVENTS_BY_USER_QUERY, this::mapRowToEvent, userId);
        log.info("Найдено {} событий", events.size());
        return events;
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        String eventTypeStr = rs.getString("event_type");
        String operationStr = rs.getString("operation");
        log.info("Маппинг события: event_type={}, operation={}", eventTypeStr, operationStr);
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(eventTypeStr))
                .operation(Operation.valueOf(operationStr))
                .entityId(rs.getLong("entity_id"))
                .timestamp(rs.getLong("timestamp"))
                .build();
    }
}