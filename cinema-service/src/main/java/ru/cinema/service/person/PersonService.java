package ru.cinema.service.person;

import org.springframework.stereotype.Service;
import ru.cinema.dto.person.PersonResponse;
import ru.cinema.repository.PersonRepository;

import java.util.List;

/**
 * PersonService — справочник людей (актёров/режиссёров) для use-case
 * Этапа 2 «поиск по актёрам/режиссёрам».
 */
@Service
public class PersonService {

    private final PersonRepository personRepo;

    public PersonService(PersonRepository personRepo) { this.personRepo = personRepo; }

    public List<PersonResponse> search(String q) {
        if (q == null || q.isBlank()) return List.of();
        return personRepo.findByNameContainingIgnoreCase(q.trim()).stream()
                .map(PersonResponse::of).toList();
    }

    public List<Long> contentIdsByPersonName(String q, String role) {
        if (q == null || q.isBlank()) return List.of();
        return personRepo.findContentIdsByPersonName(q.trim(), role);
    }
}
