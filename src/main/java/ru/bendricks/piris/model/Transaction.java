package ru.bendricks.piris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "iban")
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "iban")
    private Account recipient;

    @Column(name = "amount")
    private long amount;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

}
