package ru.bendricks.piris.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Citizenship;
import ru.bendricks.piris.model.City;
import ru.bendricks.piris.model.Disability;
import ru.bendricks.piris.model.MaritalStatus;
import ru.bendricks.piris.model.Obligation;
import ru.bendricks.piris.model.Sex;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.model.UserRole;
import ru.bendricks.piris.repository.AccountRepository;
import ru.bendricks.piris.repository.ObligationRepository;
import ru.bendricks.piris.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log
public class InitConfig {

    private final ObligationRepository obligationRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Value("${sfrbUUID}")
    private UUID sfrbUUID;

    @PostConstruct
    public void init() {
//        UUID sfrbAccUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User adminUser = new User(1L, UserRole.ROLE_ADMIN, "$2a$12$eGOIoJpMlGz2HQnRuK0EueD1mZOk2ALUm5QZBpNbRWwGQjyWFouEq",
                "Банк", "Банк", "Банк", LocalDate.now(), Sex.ATTACK_HELICOPTER,
                "bank", "bank", LocalDate.MAX, "bank", "bank",
                City.MINSK, "bank", "bank", "bank",
                "bank@bank.bank", "bank", MaritalStatus.SINGLE,
                Citizenship.REPUBLIC_OF_BELARUS, Disability.NO, false, 0L, null);
        if (userRepository.findAll().isEmpty()) {
            log.info("Не был найден ни один аккаунт. Будет создана запись");
            userRepository.save(adminUser);
        }
        if (accountRepository.findById(sfrbUUID).isEmpty()) {
            log.info("Счет СФРБ не был найден. Будет создана запись");
            accountRepository.save(new Account(sfrbUUID, adminUser, 10000000000000L));
        }
    }

}