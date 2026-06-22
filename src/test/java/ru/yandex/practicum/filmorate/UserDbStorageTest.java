package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private static final AtomicLong counter = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testAddUser() {
        User user = createTestUser("test@example.com", "testLogin");
        User saved = userStorage.add(user);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testUpdateUser() {
        User user = createTestUser("test@example.com", "testLogin");
        User saved = userStorage.add(user);
        saved.setName("Updated Name");
        User updated = userStorage.update(saved);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testGetUserById() {
        User user = createTestUser("test@example.com", "testLogin");
        User saved = userStorage.add(user);
        Optional<User> found = userStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void testGetAllUsers() {
        User user1 = createTestUser("user1@example.com", "user1");
        User user2 = createTestUser("user2@example.com", "user2");
        userStorage.add(user1);
        userStorage.add(user2);
        Collection<User> users = userStorage.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void testAddFriend() {
        long unique1 = counter.getAndIncrement();
        long unique2 = counter.getAndIncrement();

        User userA = createTestUser("a" + unique1 + "@example.com", "userA" + unique1);
        User userB = createTestUser("b" + unique2 + "@example.com", "userB" + unique2);
        User savedA = userStorage.add(userA);
        User savedB = userStorage.add(userB);

        userStorage.addFriend(savedA.getId(), savedB.getId());

        Collection<User> friends = userStorage.getFriends(savedA.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(savedB.getId());
    }

    @Test
    void testRemoveFriend() {
        long unique1 = counter.getAndIncrement();
        long unique2 = counter.getAndIncrement();

        User userA = createTestUser("a" + unique1 + "@example.com", "userA" + unique1);
        User userB = createTestUser("b" + unique2 + "@example.com", "userB" + unique2);
        User savedA = userStorage.add(userA);
        User savedB = userStorage.add(userB);

        userStorage.addFriend(savedA.getId(), savedB.getId());
        userStorage.removeFriend(savedA.getId(), savedB.getId());

        Collection<User> friends = userStorage.getFriends(savedA.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetFriends() {
        long unique1 = counter.getAndIncrement();
        long unique2 = counter.getAndIncrement();

        User userA = createTestUser("a" + unique1 + "@example.com", "userA" + unique1);
        User userB = createTestUser("b" + unique2 + "@example.com", "userB" + unique2);
        User savedA = userStorage.add(userA);
        User savedB = userStorage.add(userB);

        userStorage.addFriend(savedA.getId(), savedB.getId());

        Collection<User> friends = userStorage.getFriends(savedA.getId());
        assertThat(friends).hasSize(1);
    }

    @Test
    void testGetCommonFriends() {
        long unique1 = counter.getAndIncrement();
        long unique2 = counter.getAndIncrement();
        long unique3 = counter.getAndIncrement();

        User userA = createTestUser("a" + unique1 + "@example.com", "userA" + unique1);
        User userB = createTestUser("b" + unique2 + "@example.com", "userB" + unique2);
        User common = createTestUser("common" + unique3 + "@example.com", "common" + unique3);

        User savedA = userStorage.add(userA);
        User savedB = userStorage.add(userB);
        User savedCommon = userStorage.add(common);

        userStorage.addFriend(savedA.getId(), savedCommon.getId());
        userStorage.addFriend(savedB.getId(), savedCommon.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(savedA.getId(), savedB.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(savedCommon.getId());
    }
}