package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private Long testUserId;
    private static final AtomicLong counter = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM users");

        // Сбрасываем счётчик автоинкремента
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

        long unique = counter.getAndIncrement();
        User user = new User();
        user.setEmail("test" + unique + "@example.com");
        user.setLogin("testUser" + unique);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User saved = userStorage.add(user);
        testUserId = saved.getId();
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void testAddFilm() {
        Film film = createTestFilm("Test Film");
        Film saved = filmStorage.add(film);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testUpdateFilm() {
        Film film = createTestFilm("Test Film");
        Film saved = filmStorage.add(film);
        saved.setName("Updated Name");
        Film updated = filmStorage.update(saved);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testGetFilmById() {
        Film film = createTestFilm("Test Film");
        Film saved = filmStorage.add(film);
        Optional<Film> found = filmStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void testGetAllFilms() {
        Film film1 = createTestFilm("Film 1");
        Film film2 = createTestFilm("Film 2");
        filmStorage.add(film1);
        filmStorage.add(film2);
        Collection<Film> films = filmStorage.getAll();
        assertThat(films).hasSize(2);
    }

    @Test
    void testAddLike() {
        Film film = createTestFilm("Test Film");
        Film saved = filmStorage.add(film);
        filmStorage.addLike(saved.getId(), testUserId);
        Optional<Film> found = filmStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).contains(testUserId);
    }

    @Test
    void testRemoveLike() {
        Film film = createTestFilm("Test Film");
        Film saved = filmStorage.add(film);
        filmStorage.addLike(saved.getId(), testUserId);
        filmStorage.removeLike(saved.getId(), testUserId);
        Optional<Film> found = filmStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).doesNotContain(testUserId);
    }

    @Test
    void testGetTopFilms() {
        Film unpopular = createTestFilm("Unpopular Film");
        Film popular = createTestFilm("Popular Film");

        Film savedUnpopular = filmStorage.add(unpopular);
        Film savedPopular = filmStorage.add(popular);

        // Ставим 2 лайка популярному фильму
        filmStorage.addLike(savedPopular.getId(), testUserId);

        // Создаём второго пользователя для второго лайка
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User savedUser2 = userStorage.add(user2);
        filmStorage.addLike(savedPopular.getId(), savedUser2.getId());

        Collection<Film> topFilms = filmStorage.getTopFilms(1);
        assertThat(topFilms).hasSize(1);
        assertThat(topFilms.iterator().next().getName()).isEqualTo("Popular Film");
    }

    @Test
    void testFilmWithMpaAndGenres() {
        Film film = createTestFilm("Test Film");

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Set<Genre> genres = new LinkedHashSet<>();
        Genre genre = new Genre();
        genre.setId(1);
        genres.add(genre);
        film.setGenres(genres);

        Film saved = filmStorage.add(film);
        Optional<Film> found = filmStorage.getById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMpa()).isNotNull();
        assertThat(found.get().getMpa().getId()).isEqualTo(1);
        assertThat(found.get().getGenres()).isNotEmpty();
    }

    @Test
    void testFilmGetCommon() {
        Film film1 = createTestFilm("Common Film 1");
        Film savedFilm1 = filmStorage.add(film1);

        Film film2 = createTestFilm("Common Film 2");
        Film savedFilm2 = filmStorage.add(film2);

        Film film3 = createTestFilm("Unique Film");
        Film savedFilm3 = filmStorage.add(film3);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User savedUser2 = userStorage.add(user2);
        Long user2Id = savedUser2.getId();

        filmStorage.addLike(savedFilm1.getId(), testUserId);
        filmStorage.addLike(savedFilm1.getId(), user2Id);

        filmStorage.addLike(savedFilm2.getId(), testUserId);
        filmStorage.addLike(savedFilm2.getId(), user2Id);

        filmStorage.addLike(savedFilm3.getId(), testUserId);

        List<Film> commonFilms = filmStorage.getCommonFilms(testUserId, user2Id);

        assertThat(commonFilms).hasSize(2);

        Set<Long> commonFilmIds = commonFilms.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        assertThat(commonFilmIds).contains(savedFilm1.getId(), savedFilm2.getId());

        assertThat(commonFilmIds).doesNotContain(savedFilm3.getId());

        Set<String> commonFilmNames = commonFilms.stream()
                .map(Film::getName)
                .collect(Collectors.toSet());
        assertThat(commonFilmNames).contains("Common Film 1", "Common Film 2");
    }
}