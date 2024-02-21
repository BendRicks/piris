package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.model.UserRole;
import ru.bendricks.piris.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAllByUserRole(UserRole.ROLE_USER);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    public User registerUser(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPassportId()));
        user.setUserRole(UserRole.ROLE_USER);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user){
        user.setPasswordHash(userRepository.findById(user.getId()).map(User::getPasswordHash).orElse(null));
        return userRepository.save(user);
    }

    public boolean isEmailUsed(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }

    public boolean isPassportIdUsed(String passportId){
        return userRepository.findUserByPassportId(passportId).isPresent();
    }

    public boolean isPassportSerialUsed(String passportSerial){
        return userRepository.findUserByPassportSerial(passportSerial).isPresent();
    }

    public boolean isMobilePhoneNumberUsed(String mobilePhoneNumber){
        return userRepository.findUserByMobilePhoneNumber(mobilePhoneNumber).isPresent();
    }

}
