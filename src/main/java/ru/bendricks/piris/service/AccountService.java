package ru.bendricks.piris.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.model.Currency;
import ru.bendricks.piris.repository.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Log
@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${sfrbIBANByn}")
    private String sfrbIBANByn;
    @Value("${sfrbIBANUsd}")
    private String sfrbIBANUsd;
    @Value("${sfrbIBANEur}")
    private String sfrbIBANEur;
    @Value("${cashIBANByn}")
    private String cashIBANByn;
    @Value("${cashIBANUsd}")
    private String cashIBANUsd;
    @Value("${cashIBANEur}")
    private String cashIBANEur;

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final Set<Integer> permittedSenderAccountTypes = Set.of(3014, 3470, 2400, 1010, 7327);

    @Transactional
    public Transaction putMoneyCard(Long amount) throws Exception {
        var account = getAccountById(SecurityContextHolder.getContext().getAuthentication().getName());
        var cashAcc = getCashAccount(account.getCurrency());
        cashAcc.ifPresent(acc -> acc.setBalance(acc.getBalance() + amount));
        CustomUserDetails adminUser = new CustomUserDetails(userRepository.findUserByEmail("bank@bank.bank").orElse(null));
        return transferMoney(new Transaction(null, cashAcc.orElseThrow(() -> new Exception("Счет кассы не был найден")), account, amount, LocalDateTime.now(), account.getCurrency()), adminUser);
    }

    @Transactional
    public Transaction withdrawMoneyCard(Long amount) throws Exception {
        var account = getAccountById(SecurityContextHolder.getContext().getAuthentication().getName());
        var cashAcc = getCashAccount(account.getCurrency());
        CustomUserDetails adminUser = new CustomUserDetails(userRepository.findUserByEmail("bank@bank.bank").orElse(null));
        var transaction = transferMoney(new Transaction(null, account, cashAcc.orElseThrow(() -> new Exception("Счет кассы не был найден")), amount, LocalDateTime.now(), account.getCurrency()), adminUser);
        cashAcc.ifPresent(acc -> acc.setBalance(acc.getBalance() - amount));
        return transaction;
    }

    @Transactional
    public Transaction transferMoney(Transaction transaction, CustomUserDetails userDetails) throws Exception {
        Account senderAccount = accountRepository.findById(transaction.getSender().getIban()).orElseThrow(() -> new Exception("Счёт отправителя не был найден"));
        Account recipientAccount = accountRepository.findById(transaction.getRecipient().getIban()).orElseThrow(() -> new Exception("Счёт получателя не был найден"));
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            if (!senderAccount.getOwner().getId().equals(userDetails.getId()))
                throw new Exception("Ты хто такой?");
            if (senderAccount.getStatus() == RecordStatus.CLOSED)
                throw new Exception("С данного счёта нельзя перевести деньги так как он закрыт");
            if (!permittedSenderAccountTypes.contains(senderAccount.getAccountType().getCode()) && !(senderAccount.getAccountType().getCode() == 3404 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE))
                throw new Exception("С данного счёта нельзя перевести деньги");
            if (recipientAccount.getStatus() == RecordStatus.CLOSED || (recipientAccount.getStatus() == RecordStatus.END_OF_SERVICE && recipientAccount.getAccountType().getCode() != 2470))
                throw new Exception("На данный счёт нельзя перевести деньги");
            if (senderAccount.getBalance() < transaction.getAmount())
                throw new Exception("Недостаточно денег на балансе отправителя");
        }
        if (senderAccount.getCurrency() != recipientAccount.getCurrency())
            throw new Exception("Невозможно перевести деньги на счёт с другой валютой");
        senderAccount.setBalance(senderAccount.getBalance() - transaction.getAmount());
        if (senderAccount.getBalance() == 0 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE) {
            senderAccount.setStatus(RecordStatus.CLOSED);
            var parentObligation = Optional.of(senderAccount.getParentObligationAsMainAccount()).orElse(senderAccount.getParentObligationAsPercentAccount());
            if (parentObligation.getMainAccount().getStatus() == RecordStatus.CLOSED
                    && parentObligation.getPercentAccount().getStatus() == RecordStatus.CLOSED) {
                parentObligation.setStatus(RecordStatus.CLOSED);
            }
        }
        recipientAccount.setBalance(recipientAccount.getBalance() + transaction.getAmount());
        if (recipientAccount.getBalance() >= 0 && recipientAccount.getStatus() == RecordStatus.END_OF_SERVICE) {
            recipientAccount.setStatus(RecordStatus.CLOSED);
            var parentObligation = Optional.of(senderAccount.getParentObligationAsMainAccount()).orElse(senderAccount.getParentObligationAsPercentAccount());
            if (parentObligation.getMainAccount().getStatus() == RecordStatus.CLOSED
                    && parentObligation.getPercentAccount().getStatus() == RecordStatus.CLOSED) {
                parentObligation.setStatus(RecordStatus.CLOSED);
            }
        }
        transaction.setSender(senderAccount);
        transaction.setRecipient(recipientAccount);
        transaction.setTime(LocalDateTime.now());
        transaction.setCurrency(senderAccount.getCurrency());
        return transactionRepository.save(transaction);
    }

    private String generateIban() {
        StringBuilder sb;
        do {
            var rand = new Random();
            sb = new StringBuilder("BY");
            sb.append(rand.nextInt(10)).append(rand.nextInt(10)).append("MTBK")
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10));
        } while (accountRepository.existsById(sb.toString()));
        return sb.toString();
    }

    public List<Account> getAccountsByUserId(Long id) {
        return accountRepository.findAllByOwnerId(id);
    }

    public Account getAccountById(String iban) {
        return accountRepository.findById(iban).orElse(null);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void createDepositAccounts(Obligation obligation) {
        var mainIban = generateIban();
        var percentIban = generateIban();
        var mainAccount = new Account(mainIban, "Депозитный счёт", accountTypeRepository.findById(3404).orElse(null),
                RecordStatus.ACTIVE, obligation.getOwner(), 0, obligation.getCurrency(), obligation,
                null, null, null, null);
        var percentAccount = new Account(percentIban, "Процентный счёт по депозиту " + mainIban,
                accountTypeRepository.findById(3470).orElse(null), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), null, obligation, null, null, null);
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void createCreditAccounts(Obligation obligation) {
        var mainIban = generateIban();
        var percentIban = generateIban();
        var mainAccount = new Account(mainIban, "Кредитный счёт", accountTypeRepository.findById(2400).orElse(null),
                RecordStatus.ACTIVE, obligation.getOwner(), 0, obligation.getCurrency(), obligation,
                null, null, null, null);
        var percentAccount = new Account(percentIban, "Процентный счёт по кредиту " + mainIban,
                accountTypeRepository.findById(2470).orElse(null), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), null, obligation, null, null, null);
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Account> getSfrbAccount(Currency currency) {
        return switch (currency) {
            case BYN -> accountRepository.findById(sfrbIBANByn);
            case USD -> accountRepository.findById(sfrbIBANUsd);
            case EUR -> accountRepository.findById(sfrbIBANEur);
        };
    }

    public Optional<Account> getCashAccount(Currency currency) {
        return switch (currency) {
            case BYN -> accountRepository.findById(cashIBANByn);
            case USD -> accountRepository.findById(cashIBANUsd);
            case EUR -> accountRepository.findById(cashIBANEur);
        };
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account createPaymentAccount(Account account) {
        account.setIban(generateIban());
        account.setAccountType(accountTypeRepository.findById(3014).orElse(null));
        account.setBalance(0);
        account.setStatus(RecordStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> getPaymentAccounts(Long id, Currency currency) {
        return accountRepository.findAllByOwnerIdAndAccountTypeCodeAndCurrency(id, 3014, currency);
    }

    private Optional<Card> getCard(Long id) {
        return cardRepository.findById(id);
    }

    @Transactional
    public boolean authenticateCard(HttpServletRequest req, MultiValueMap<String, String> cardData, Model model) {
        var cardNumber = Long.valueOf(cardData.get("card_number").stream().findFirst().orElse("0"));
        var pin = cardData.get("pin").stream().findFirst().orElse("0");
        var card = getCard(cardNumber);
        if (card.isPresent()) {
            if (card.get().getAttemptsBeforeBlock() == 0) {
                model.addAttribute("errorMessage", "Карта заблокирована, обратитесь в банк для разблокировки");
                return false;
            }
            if (card.get().getAccount().getStatus() == RecordStatus.CLOSED){
                model.addAttribute("errorMessage", "Счёт, обслуживаемый картой, был закрыт. Обратитесь в банк за подробностями");
                return false;
            }
            if (passwordEncoder.matches(pin, card.get().getPinHash())) {
                card.get().setAttemptsBeforeBlock((byte) 3);
                org.springframework.security.core.userdetails.User user = new User(card.get().getAccount().getIban(), card.get().getPinHash(), true, true, true, true, List.of(new SimpleGrantedAuthority("ROLE_ATM")));
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
                var sc = SecurityContextHolder.getContext();
                sc.setAuthentication(authentication);
                var session = req.getSession(true);
                session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);
                return true;
            } else {
                card.get().setAttemptsBeforeBlock((byte) (card.get().getAttemptsBeforeBlock() - 1));
                model.addAttribute("errorMessage", "Неверный PIN, осталось попыток - " + card.get().getAttemptsBeforeBlock());
                return false;
            }
        } else {
            model.addAttribute("errorMessage", "Карта не найдена, проверьте корректность вводимых данных");
            return false;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void createPaymentCard(String iban) throws Exception {
        var acceptableAccountTypes = Set.of(3014 ,2400);
        var account = accountRepository.findById(iban).orElseThrow(() -> new Exception("Не найдено счета с таким iban"));
        if (!acceptableAccountTypes.contains(account.getAccountType().getCode()))
            throw new Exception("Для данного счёта невозможно создать карту");
        cardRepository.save(Card.builder()
                .account(account).number(generatePlasticCardNumber()).attemptsBeforeBlock((byte)3)
                .cvcCcvHash(passwordEncoder.encode(generateRandDigitsString(3)))
                .pinHash(passwordEncoder.encode(generateRandDigitsString(4))).build());
    }

    @Transactional
    public String updateCardCredentials(Long cardNumber, CustomUserDetails userDetails) throws Exception {
        var card = cardRepository.findById(cardNumber).orElseThrow(() -> new Exception("Карта с таким номером не найдена"));
        if (!Objects.equals(card.getAccount().getOwner().getId(), userDetails.getId()))
            throw new Exception("Ты хто такой, чтоб это делать?");
        var newCvcCCv = generateRandDigitsString(3);
        var newPin = generateRandDigitsString(4);
        card.setCvcCcvHash(passwordEncoder.encode(newCvcCCv));
        card.setPinHash(passwordEncoder.encode(newPin));
        return "CVC/CCV:"+newCvcCCv+";PIN:"+newPin;
    }

    private String generateRandDigitsString(int length){
        var rand = new Random();
        var sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Math.abs(rand.nextInt() % 10));
        }
        return sb.toString();
    }

    private static long generatePlasticCardNumber() {
        long partCardNumber = (long) (Math.random() * 1_000_000_000_000_000L);
        String cardNumber = Long.toString(partCardNumber);
        int checkDigit = getLuhnCheckDigit(cardNumber);
        cardNumber += Integer.toString(checkDigit);
        return Long.parseLong(cardNumber);
    }

    private static int getLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

}
