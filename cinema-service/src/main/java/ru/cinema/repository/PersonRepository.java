package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Person;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByNameContainingIgnoreCase(String namePart);

    /** Найти все content_id, где упомянут человек с данным именем (для search по актёру/режиссёру). */
    @Query(value = "SELECT cp.content_id FROM content_persons cp " +
                   "JOIN persons p ON p.id = cp.person_id " +
                   "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :namePart, '%'))" +
                   "  AND (:role IS NULL OR cp.role = :role)",
           nativeQuery = true)
    List<Long> findContentIdsByPersonName(@Param("namePart") String namePart, @Param("role") String role);
}
