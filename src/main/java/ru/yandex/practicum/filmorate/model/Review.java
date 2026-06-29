package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;

    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull(message = "Необходимо указать пользователя")
    private Long userId;

    @NotNull(message = "Необходимо указать тип отзыва")
    private Boolean isPositive;

    @NotNull(message = "Необходимо указать фильм")
    private Long filmId;

    private int useful = 0;
}