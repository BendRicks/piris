package ru.bendricks.piris.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.model.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByUserRole(UserRole role);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByPassportId(String passportId);
    Optional<User> findUserByPassportSerial(String passportSerial);
    Optional<User> findUserByMobilePhoneNumber(String mobilePhoneNumber);

}
