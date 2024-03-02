package ru.bendricks.piris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "acc_type", nullable = false)
    @ManyToOne
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
