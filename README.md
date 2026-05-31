# Filmorate API

REST API для управления фильмами, пользователями, друзьями и лайками. Хранение в памяти, обмен JSON.

## Запуск

Запустите `ru.yandex.practicum.filmorate.FilmorateApplication`

Сервер стартует на `http://localhost:8080`

## Эндпоинты

### Фильмы

| Метод | Путь | Описание |
|-------|------|----------|
| GET | /films | все фильмы |
| GET | /films/{id} | фильм по ID |
| POST | /films | добавить фильм |
| PUT | /films | обновить фильм |
| PUT | /films/{id}/like/{userId} | поставить лайк |
| DELETE | /films/{id}/like/{userId} | удалить лайк |
| GET | /films/popular?count={count} | топ N фильмов по лайкам (по умолчанию 10) |

### Пользователи

| Метод | Путь | Описание |
|-------|------|----------|
| GET | /users | все пользователи |
| GET | /users/{id} | пользователь по ID |
| POST | /users | создать пользователя |
| PUT | /users | обновить пользователя |
| PUT | /users/{id}/friends/{friendId} | добавить в друзья |
| DELETE | /users/{id}/friends/{friendId} | удалить из друзей |
| GET | /users/{id}/friends | список друзей |
| GET | /users/{id}/friends/common/{otherId} | общие друзья |

## Технологии

- Java 21
- Spring Boot 3.5
- Lombok
- Logbook (логирование HTTP запросов)

## Автор

Владислав Сакас, студент 74й когорты Java