package ru.bendricks.piris.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Currency;
import ru.bendricks.piris.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByOwner(User user);
    List<Account> findAllByOwnerId(Long id);
    List<Account> findAllByOwnerIdAndAccountTypeCodeAndCurrency(Long id, Integer code, Currency currency);

}
