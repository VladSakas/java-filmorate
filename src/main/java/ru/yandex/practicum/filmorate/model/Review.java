package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long reviewId;

    @NotBlank(message = "Отзыв не может быть пустым")
    String content;

    @NotNull(message = "Необходимо указать пользователя")
    Long userId;

    @NotNull(message = "Необходимо указать тип отзыва")
    Boolean isPositive;

    @NotNull(message = "Необходимо указать фильм")
    Long filmId;

    int useful = 0;
}
