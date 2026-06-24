package ru.yandex.practicum.filmorate.dao.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    private static final String ADD_QUERY =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM users";
    private static final String REMOVE_QUERY =
            "DELETE FROM users WHERE id = ?";
    private static final String LOAD_FRIENDS_QUERY =
            "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY =
            "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u JOIN friends f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String GET_COMMON_FRIENDS_QUERY = """ 
            SELECT u.* FROM users u
            JOIN friends f1 ON u.id = f1.friend_id AND f1.user_id = ?
            JOIN friends f2 ON u.id = f2.friend_id AND f2.user_id = ?
            """;
    private static final String FIND_BEST_MATCH_QUERY = """
            SELECT l2.user_id, COUNT(*) as common_count
            FROM likes l1
            JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id
            WHERE l1.user_id = ?
            GROUP BY l2.user_id
            ORDER BY common_count DESC
            LIMIT 1
            """;

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(ADD_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbc.update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return getById(user.getId()).orElseThrow();
    }

    @Override
    public void remove(User user) {
        jdbc.update(REMOVE_QUERY, user.getId());
    }

    @Override
    public Collection<User> getAll() {
        List<User> users = jdbc.query(FIND_ALL_QUERY, this::mapRowToUser);
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public Optional<User> getById(Long id) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, this::mapRowToUser, id);
            if (user != null) {
                loadFriends(user);
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private void loadFriends(User user) {
        List<Long> friends = jdbc.queryForList(LOAD_FRIENDS_QUERY, Long.class, user.getId());
        user.getFriends().clear();
        user.getFriends().addAll(friends);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return jdbc.query(GET_FRIENDS_QUERY, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, this::mapRowToUser, userId, otherId);
    }

    @Override
    public Optional<Long> findBestMatchUserId(Long userId) {
        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(FIND_BEST_MATCH_QUERY,
                            (rs, rowNum) -> rs.getLong("user_id"),
                            userId)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}