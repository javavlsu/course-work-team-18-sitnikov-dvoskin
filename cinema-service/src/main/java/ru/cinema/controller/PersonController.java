package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cinema.dto.person.PersonResponse;
import ru.cinema.service.person.PersonService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/persons")
@Tag(name = "Persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) { this.personService = personService; }

    @GetMapping
    public List<PersonResponse> search(@RequestParam(name = "q", required = false) String q) {
        return personService.search(q);
    }
}
