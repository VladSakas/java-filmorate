package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class Film {
    private final Set<Long> likes = new HashSet<>();
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}