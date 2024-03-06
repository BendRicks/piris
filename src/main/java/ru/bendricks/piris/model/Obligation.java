package ru.bendricks.piris.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

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

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "obligation_plan_id", referencedColumnName = "id")
//    @JsonManagedReference("oblig-plan")
    private ObligationPlan obligationPlan;

    @Column(name = "obligation_type")
    @Enumerated(EnumType.ORDINAL)
    private ObligationType obligationType;

    @Column(name = "status")
    private RecordStatus status;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "creation_time")
    @NotNull
    private LocalDate startTime;

    @Column(name = "end_time")
    private LocalDate endTime;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "amount")
    @Positive
    private long amount;

    @OneToOne(mappedBy = "parentObligationAsMainAccount", cascade = CascadeType.ALL)
    @JoinColumn(name = "main_acc_id", referencedColumnName = "iban")
//    @JsonManagedReference("obligation-mainacc")
    private Account mainAccount;

    @OneToOne(mappedBy = "parentObligationAsPercentAccount", cascade = CascadeType.ALL)
    @JoinColumn(name = "percent_acc_id", referencedColumnName = "iban")
//    @JsonManagedReference("obligation-peracc")
    private Account percentAccount;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
//    @JsonBackReference("user-obligations")
    private User owner;

}
