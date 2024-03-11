package ru.bendricks.piris.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @Column(name = "number")
    private long number;

    @Column(name = "cvc_ccv")
    private String cvcCcvHash;

    @Column(name = "pin_hash")
    private String pinHash;

    @Column(name = "attempts_before_block")
    private byte attemptsBeforeBlock;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "iban")
    @JsonIgnore
    private Account account;

}
