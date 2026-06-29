package ru.yandex.practicum.filmorate.exception;

public class MissingUserIdException extends RuntimeException {
    public MissingUserIdException(String message) {
        super(message);
    }
}