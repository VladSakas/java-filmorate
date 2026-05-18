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
public class User {
    private final Set<Long> friends = new HashSet<>();
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}