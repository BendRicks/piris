package ru.bendricks.piris.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bendricks.piris.model.Obligation;

import java.util.List;

@Repository
public interface ObligationRepository extends JpaRepository<Obligation, Long> {

    List<Obligation> findAllByOwnerId(Long id);

    boolean existsByContractNumber(String contractNumber);

}