package ru.bendricks.piris.model;

import jakarta.persistence.*;
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

//    @Id
//    private UUID id;

    @Id
    private String iban;

    @Column(name = "name")
    private String name;

//    @Column(name = "acc_type", nullable = false)
    @ManyToOne
    @JoinColumn(name = "acc_type_code", referencedColumnName = "code")
    private AccountType accountType;

    @Column(name = "status")
    private RecordStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "balance", nullable = false)
    private long balance;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToOne
    private Obligation parentObligation;

    @OneToMany(mappedBy = "sender")
    private List<Transaction> transactionsAsSender;

    @OneToMany(mappedBy = "recipient")
    private List<Transaction> transactionsAsRecipient;

}
