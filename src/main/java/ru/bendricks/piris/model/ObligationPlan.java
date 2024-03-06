package ru.bendricks.piris.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "obligation_plan")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ObligationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private RecordStatus status;

    @Column(name = "obligation_type")
    @Enumerated(EnumType.ORDINAL)
    private ObligationType obligationType;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "name")
    private String name;

    @Column(name = "plan_percent")
    private double planPercent;

    @Column(name = "months")
    private int months;

    @OneToMany(mappedBy = "obligationPlan")
    @JsonIgnore
    List<Obligation> obligations;

}
