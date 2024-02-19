package ru.bendricks.piris.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.bendricks.piris.model.User;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        if (LocalDate.now().until(user.getBirthDate()).getYears() < 16)
            errors.rejectValue("birthDate", "not.old.enough", "Младше 16 лет");
        if (userService.isEmailUsed(user.getEmail()))
            errors.rejectValue("email", "already.in.use", "Данный email уже используется");
        if (userService.isMobilePhoneNumberUsed(user.getMobilePhoneNumber()))
            errors.rejectValue("mobilePhoneNumber", "already.in.use", "Данный мобильный телефон уже используется");
        if (userService.isPassportSerialUsed(user.getPassportSerial()))
            errors.rejectValue("passportSerial", "already.in.use", "Данный номер паспорта уже используется");
        if (userService.isPassportIdUsed(user.getPassportId()))
            errors.rejectValue("passportId", "already.in.use", "Данный идентификационный номер уже используется");
    }
}
