package ru.bendricks.piris.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "balance", nullable = false)
    private long balance;

//    @Column(name = "acc_type", nullable = false)
//    private AccountType accountType;

//    @OneToOne(mappedBy = "parentAccount", cascade = CascadeType.ALL)
//    @JoinColumn(name = "parent_acc_id", referencedColumnName = "id")
//    private Account parentAccount;
//
//    @OneToOne(mappedBy = "childAccount", cascade = CascadeType.ALL)
//    @JoinColumn(name = "child_acc_id", referencedColumnName = "id")
//    private Account childAccount;

}
