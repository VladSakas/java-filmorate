package ru.yandex.practicum.filmorate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    String status;
    String description;
}