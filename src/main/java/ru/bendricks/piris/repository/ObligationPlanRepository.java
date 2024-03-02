package ru.bendricks.piris.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bendricks.piris.model.ObligationPlan;
import ru.bendricks.piris.model.ObligationType;

@Repository
public interface ObligationPlanRepository extends JpaRepository<ObligationPlan, Long> {
}
