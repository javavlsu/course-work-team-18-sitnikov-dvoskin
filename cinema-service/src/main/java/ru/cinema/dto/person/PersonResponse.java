package ru.cinema.dto.person;

import ru.cinema.model.Person;

public record PersonResponse(Long id, String name, String photoUrl) {
    public static PersonResponse of(Person p) {
        return new PersonResponse(p.getId(), p.getName(), p.getPhotoUrl());
    }
}
