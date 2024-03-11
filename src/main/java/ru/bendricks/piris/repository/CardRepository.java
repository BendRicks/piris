package ru.bendricks.piris.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bendricks.piris.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}
