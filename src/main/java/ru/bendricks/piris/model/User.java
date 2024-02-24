package ru.bendricks.piris.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "surname", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = "[А-Яа-я]+", message = "Может состоять только из букв кириллицы")
    private String surname;

    @Column(name = "name", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = "[А-Яа-я]+", message = "Может состоять только из букв кириллицы")
    private String name;

    @Column(name = "givenName", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = "[А-Яа-я]+", message = "Может состоять только из букв кириллицы")
    private String givenName;

    @Column(name = "birthDate", nullable = false)
    @NotNull(message = "Не может быть пустым")
    private LocalDate birthDate;

    @Column(name = "sex", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "Не может быть пустым")
    private Sex sex;

    @Column(name = "passport_serial", nullable = false, length = 10, unique = true)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = "[A-Z]{2}\\d{7}", message = "Должно соответствовать шаблону МР1234567")
    private String passportSerial;

    @Column(name = "competent_organ", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    private String competentOrgan;

    @Column(name = "date_of_issue", nullable = false)
    @NotNull(message = "Не может быть пустым")
    private LocalDate dateOfIssue;

    @Column(name = "passport_id", nullable = false, unique = true)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = "\\d{7}[A-Z]\\d{3}[A-Z]{2}\\d", message = "Должно соответствовать шаблону 5111200A001PB8")
    private String passportId;

    @Column(name = "birth_location", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    private String birthLocation;

    @Column(name = "city_of_residence", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "Не может быть пустым")
    private City cityOfResidence;

    @Column(name = "address_of_living", nullable = false)
    @NotEmpty(message = "Не может быть пустым")
    private String addressOfLiving;

    @Column(name = "home_phone_number")
    @Pattern(regexp = "(\\d{7})?", message = "Должен состоять из 7 цифр 1234567")
    private String homePhoneNumber;

    @Column(name = "mobile_phone_number", unique = true)
    @Pattern(regexp = "(\\d{9})?", message = "Должен состоять из 9 цифр 123456789, где первые 2 - код (25, 29, 33, 44)")
    private String mobilePhoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    @NotEmpty(message = "Не может быть пустым")
    @Pattern(regexp = ".+@.+\\..+", message = "Некорректный адрес")
    private String email;

    @Column(name = "place_of_residence", nullable = false)
    private String placeOfResidence;

    @Column(name = "marital_status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private MaritalStatus maritalStatus;

    @Column(name = "citizenship", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Citizenship citizenship;

    @Column(name = "disability", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Disability disability;

    @Column(name = "pensioner", nullable = false)
    private boolean pensioner;

    @Column(name = "monthly_income")
    private long monthlyIncome;

    @OneToMany(mappedBy = "owner")
    private List<Account> accounts;

}
