package ru.bendricks.piris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

//public enum AccountType {
@Entity
@Table(name = "account_type")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountType {

    @Id
    @Column(name = "code")
    private int code;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "accountType")
    private List<Account> accounts;

}
