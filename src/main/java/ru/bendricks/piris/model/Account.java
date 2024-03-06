package ru.bendricks.piris.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Account {

    @Id
    private String iban;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "acc_type_code", referencedColumnName = "code")
//    @JsonManagedReference("acc-type")
    private AccountType accountType;

    @Column(name = "status")
    private RecordStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
//    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User owner;

    @Column(name = "balance", nullable = false)
    private long balance;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToOne
    @JsonIgnore
    private Obligation parentObligationAsMainAccount;

    @OneToOne
    @JsonIgnore
    private Obligation parentObligationAsPercentAccount;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactionsAsSender;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactionsAsRecipient;

}
