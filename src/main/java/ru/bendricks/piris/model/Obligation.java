package ru.bendricks.piris.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "obligation")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Obligation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "obligation_plan_id", referencedColumnName = "id")
    private ObligationPlan obligationPlan;

    @Column(name = "obligation_type")
    @Enumerated(EnumType.ORDINAL)
    private ObligationType obligationType;

    @Column(name = "status")
    private RecordStatus status;

    @Column(name = "contract_number")
    @NotBlank
    private String contractNumber;

    @Column(name = "creation_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "amount")
    private long amount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "main_acc_id", referencedColumnName = "iban")
    private Account mainAccount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "percent_acc_id", referencedColumnName = "iban")
    private Account percentAccount;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

}
