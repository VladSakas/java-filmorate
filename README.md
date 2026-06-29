# Filmorate API

REST API для управления фильмами, пользователями, друзьями и лайками. Хранение в памяти, обмен JSON.

## Запуск

Запустите `ru.yandex.practicum.filmorate.FilmorateApplication`

Сервер стартует на `http://localhost:8080`

## Эндпоинты

### Фильмы

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/films` | все фильмы |
| GET | `/films/{id}` | фильм по ID |
| POST | `/films` | добавить фильм |
| PUT | `/films` | обновить фильм |
| DELETE | `/films/{id}` | удалить фильм |
| PUT | `/films/{id}/like/{userId}` | поставить лайк |
| DELETE | `/films/{id}/like/{userId}` | удалить лайк |
| GET | `/films/popular?count={count}&genreId={genreId}&year={year}` | топ N фильмов по лайкам с фильтрацией по жанру и году |
| GET | `/films/director/{directorId}?sortBy={sortBy}` | фильмы режиссёра (сортировка по `year` или `likes`) |
| GET | `/films/common?userId={userId}&friendId={friendId}` | общие фильмы двух пользователей |
| GET | `/films/search?query={query}&by={title,director}` | поиск фильмов по названию и/или режиссёру |

### Пользователи

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/users` | все пользователи |
| GET | `/users/{id}` | пользователь по ID |
| POST | `/users` | создать пользователя |
| PUT | `/users` | обновить пользователя |
| DELETE | `/users/{id}` | удалить пользователя |
| PUT | `/users/{id}/friends/{friendId}` | добавить в друзья |
| DELETE | `/users/{id}/friends/{friendId}` | удалить из друзей |
| GET | `/users/{id}/friends` | список друзей |
| GET | `/users/{id}/friends/common/{otherId}` | общие друзья |
| GET | `/users/{id}/recommendations` | рекомендации фильмов |
| GET | `/users/{id}/feed` | лента событий пользователя |

### Отзывы

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/reviews` | создать отзыв |
| PUT | `/reviews` | обновить отзыв |
| DELETE | `/reviews/{id}` | удалить отзыв |
| GET | `/reviews/{id}` | отзыв по ID |
| GET | `/reviews?filmId={filmId}&count={count}` | отзывы к фильму (по умолчанию 10) |
| PUT | `/reviews/{id}/like/{userId}` | поставить лайк отзыву |
| PUT | `/reviews/{id}/dislike/{userId}` | поставить дизлайк отзыву |
| DELETE | `/reviews/{id}/like/{userId}` | удалить лайк отзыва |
| DELETE | `/reviews/{id}/dislike/{userId}` | удалить дизлайк отзыва |

### Режиссёры

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/directors` | добавить режиссёра |
| GET | `/directors` | все режиссёры |
| GET | `/directors/{id}` | режиссёр по ID |
| PUT | `/directors` | обновить режиссёра |
| DELETE | `/directors/{id}` | удалить режиссёра |

### Жанры

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/genres` | все жанры |
| GET | `/genres/{id}` | жанр по ID |

### Рейтинги MPA

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/mpa` | все рейтинги MPA |
| GET | `/mpa/{id}` | рейтинг MPA по ID |

---
## Схема базы данных
![Схема БL](database-schema-new.png)
### Примеры запросов
#### 1. Все фильмы с рейтингом MPA:
```sql
SELECT
    id,
    name,
    duration,
    mpa_rating_id
FROM
    films
WHERE
    mpa_rating_id IN (
        SELECT
            id
        FROM
            mpa_rating
    );
```

#### 2. Топ 10 популярных фильмов:
```sql
SELECT
    id,
    name
FROM
    films
WHERE
    id IN (
        SELECT
            film_id
        FROM
            likes
        GROUP BY
            film_id
        ORDER BY
            COUNT(user_id) DESC
        LIMIT 10
    );
```
#### 3. Друзья пользователя (например, с id 1):
```sql
SELECT
    id,
    login,
    name
FROM
    users
WHERE
    id IN (
        SELECT
            friend_id
        FROM
            friendship
        WHERE
            user_id = 1
          AND
            status = 'confirmed'
    );
```
#### 4. Общие друзья пользователей (например, с id 1 и 2):
```sql
SELECT
    id,
    login,
    name
FROM
    users
WHERE
    id IN (
        SELECT
            friend_id
        FROM
            friendship
        WHERE
            user_id = 1
          AND
            status = 'confirmed'
    )
  AND
    id IN (
        SELECT
            friend_id
        FROM
            friendship
        WHERE
            user_id = 2
          AND
            status = 'confirmed'
    );
```
#### 5. Получить пользователя по id (например, 1):
```sql
SELECT
    id,
    email,
    login,
    name,
    birthday
FROM
    users
WHERE
    id = 1;
```
#### 6. Лента событий пользователя (например, с id = 1):
```sql
SELECT
event_id,
user_id,
event_type,
operation,
entity_id,
timestamp
FROM user_events
WHERE user_id = 1
ORDER BY timestamp ASC;
```
#### 7. Отзывы к фильму с рейтингом:
```sql
SELECT
    r.*,
    COALESCE(SUM(ru.useful), 0) AS useful_rating
FROM reviews r
         LEFT JOIN review_useful ru ON r.id = ru.review_id
WHERE r.film_id = 1
GROUP BY r.id
ORDER BY useful_rating DESC
    LIMIT 10;
```
#### 8. Фильмы режиссёра с сортировкой по году:
```sql
SELECT
    f.*,
    m.name AS mpa_name
FROM films f
         LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
         JOIN film_directors fd ON fd.film_id = f.id
WHERE fd.director_id = 1
ORDER BY f.release_date ASC;
```
#### 9. Поиск фильмов по названию и/или режиссёру:
```sql
SELECT
    f.*,
    m.name AS mpa_name,
    COUNT(l.user_id) AS likes_count
FROM films f
         LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
         LEFT JOIN likes l ON f.id = l.film_id
         LEFT JOIN film_directors fd ON f.id = fd.film_id
         LEFT JOIN directors d ON fd.director_id = d.id
WHERE LOWER(f.name) LIKE '%query%'
   OR LOWER(d.name) LIKE '%query%'
GROUP BY f.id, f.name
ORDER BY likes_count DESC;
```

## Технологии

- Java 21
- Spring Boot 3.5
- Spring JDBC
- Lombok
- H2 Database
- Logbook (логирование HTTP запросов)

## Автор

Владислав Сакас, студент 74й когорты Java