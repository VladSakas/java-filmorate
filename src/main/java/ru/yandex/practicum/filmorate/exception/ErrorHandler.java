package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConditionNotMet(final ConditionsNotMetException e) {
        log.error("Условие не выполнено: {}", e.getMessage());
        return new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.error("Ресурс не найден: {}", e.getMessage());
        return new ErrorResponse(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingUserId(final MissingUserIdException e) {
        log.warn("Ошибка: {}", e.getMessage());
        return new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoResource(final NoResourceFoundException e) {
        String message = e.getMessage();
        log.warn("Запрос к несуществующему ресурсу: {}", message);

        if (message != null && message.contains("users/feed")) {
            return new ErrorResponse(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "ID пользователя обязателен. Для получения ленты событий используйте: /users/{id}/feed"
            );
        }
        return new ErrorResponse(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                "Запрашиваемый ресурс не существует"
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(final Throwable e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return new ErrorResponse(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "Произошла непредвиденная ошибка");
    }
}