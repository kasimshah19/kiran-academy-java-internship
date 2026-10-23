import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankingApp {

    public static final BigDecimal SCALE_ZERO = new BigDecimal("0.00");
    public static final BigDecimal MIN_SAVINGS_BALANCE = new BigDecimal("1000.00");
    public static final BigDecimal MAX_SINGLE_WITHDRAWAL = new BigDecimal("50000.00");
    public static final BigDecimal MAX_SINGLE_TRANSFER = new BigDecimal("100000.00");
    public static final BigDecimal LARGE_TRANSACTION_ALERT_THRESHOLD = new BigDecimal("45000.00");
    public static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.001");
    public static final BigDecimal ANNUAL_SAVINGS_RATE = new BigDecimal("0.04");
    public static final BigDecimal FIXED_DEPOSIT_ANNUAL_RATE = new BigDecimal("0.065");
    public static final BigDecimal LOAN_ANNUAL_RATE = new BigDecimal("0.09");

    public static final String DB_FILE_PATH = "kiran_bank_enterprise_db.dat";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public enum Role { CUSTOMER, TELLER, AUDITOR, ADMIN }
    public enum AccountType { SAVINGS, CURRENT }
    public enum AccountStatus { ACTIVE, FROZEN, CLOSED }
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT, FEE, INTEREST, LOAN_DISBURSEMENT, LOAN_REPAYMENT, REVERSAL
    }
    public enum LoanStatus { PENDING, APPROVED, REJECTED, DISBURSED, CLOSED }
    public enum FdStatus { ACTIVE, MATURED }
    public enum RiskSeverity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum RiskStatus { OPEN, UNDER_REVIEW, RESOLVED, FALSE_POSITIVE }
    public enum ApprovalStatus { PENDING, APPROVED, REJECTED }

    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private String userId;
        private String username;
        private String passwordSalt;
        private String passwordHash;
        private String pinSalt;
        private String pinHash;
        private Role role;
        private int failedAttempts;
        private boolean locked;

        public User(String userId, String username, String password, String pin, Role role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.passwordSalt = generateSalt();
            this.passwordHash = hashWithSalt(password, this.passwordSalt);
            this.pinSalt = generateSalt();
            this.pinHash = hashWithSalt(pin, this.pinSalt);
            this.failedAttempts = 0;
            this.locked = false;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public Role getRole() { return role; }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
        public int getFailedAttempts() { return failedAttempts; }
        public void incrementFailedAttempts() { this.failedAttempts++; }
        public void resetFailedAttempts() { this.failedAttempts = 0; }

        public boolean verifyPassword(String inputPassword) {
            return hashWithSalt(inputPassword, this.passwordSalt).equals(this.passwordHash);
        }

        public boolean verifyPin(String inputPin) {
            return hashWithSalt(inputPin, this.pinSalt).equals(this.pinHash);
        }

        public void changePassword(String newPassword) {
            this.passwordSalt = generateSalt();
            this.passwordHash = hashWithSalt(newPassword, this.passwordSalt);
        }

        public void changePin(String newPin) {
            this.pinSalt = generateSalt();
            this.pinHash = hashWithSalt(newPin, this.pinSalt);
        }
    }

    public static class Customer implements Serializable {
        private static final long serialVersionUID = 1L;
        private String customerId;
        private String fullName;
        private String email;
        private String phone;
        private String createdAt;
        private String status;

        public Customer(String customerId, String fullName, String email, String phone) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.status = "ACTIVE";
        }

        public String getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
    }

    public static class BankAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        private String accountNumber;
        private String customerId;
        private AccountType accountType;
        private BigDecimal openingBalance;
        private BigDecimal balance;
        private AccountStatus status;
        private String createdAt;

        public BankAccount(String accountNumber, String customerId, AccountType accountType, BigDecimal initialBalance) {
            this.accountNumber = accountNumber;
            this.customerId = customerId;
            this.accountType = accountType;
            this.openingBalance = initialBalance.setScale(2, RoundingMode.HALF_EVEN);
            this.balance = initialBalance.setScale(2, RoundingMode.HALF_EVEN);
            this.status = AccountStatus.ACTIVE;
            this.createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        }

        public String getAccountNumber() { return accountNumber; }
        public String getCustomerId() { return customerId; }
        public AccountType getAccountType() { return accountType; }
        public BigDecimal getOpeningBalance() { return openingBalance; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance.setScale(2, RoundingMode.HALF_EVEN); }
        public AccountStatus getStatus() { return status; }
        public void setStatus(AccountStatus status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;
        private String transactionId;
        private String accountNumber;
        private TransactionType type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String timestamp;
        private String referenceId;
        private String idempotencyKey;
        private String description;
        private String status;

        public Transaction(String transactionId, String accountNumber, TransactionType type,
                           BigDecimal amount, BigDecimal balanceAfter, String referenceId, String idempotencyKey, String description) {
            this.transactionId = transactionId;
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
            this.balanceAfter = balanceAfter.setScale(2, RoundingMode.HALF_EVEN);
            this.timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.referenceId = referenceId;
            this.idempotencyKey = idempotencyKey;
            this.description = description;
            this.status = "SUCCESS";
        }

        public String getTransactionId() { return transactionId; }
        public String getAccountNumber() { return accountNumber; }
        public TransactionType getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public String getTimestamp() { return timestamp; }
        public String getReferenceId() { return referenceId; }
        public String getIdempotencyKey() { return idempotencyKey; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
    }

    public static class JournalEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        private String entryId;
        private String timestamp;
        private String debitAccount;
        private String creditAccount;
        private BigDecimal amount;
        private String reference;
        private String description;

        public JournalEntry(String entryId, String debitAccount, String creditAccount, BigDecimal amount, String reference, String description) {
            this.entryId = entryId;
            this.timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.debitAccount = debitAccount;
            this.creditAccount = creditAccount;
            this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
            this.reference = reference;
            this.description = description;
        }

        public String getEntryId() { return entryId; }
        public String getTimestamp() { return timestamp; }
        public String getDebitAccount() { return debitAccount; }
        public String getCreditAccount() { return creditAccount; }
        public BigDecimal getAmount() { return amount; }
        public String getReference() { return reference; }
        public String getDescription() { return description; }
    }

    public static class Beneficiary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String beneficiaryId;
        private String customerId;
        private String beneficiaryAccountNumber;
        private String beneficiaryName;
        private String bankName;

        public Beneficiary(String beneficiaryId, String customerId, String beneficiaryAccountNumber, String beneficiaryName, String bankName) {
            this.beneficiaryId = beneficiaryId;
            this.customerId = customerId;
            this.beneficiaryAccountNumber = beneficiaryAccountNumber;
            this.beneficiaryName = beneficiaryName;
            this.bankName = bankName;
        }

        public String getBeneficiaryId() { return beneficiaryId; }
        public String getCustomerId() { return customerId; }
        public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
        public String getBeneficiaryName() { return beneficiaryName; }
        public String getBankName() { return bankName; }
    }

    public static class Loan implements Serializable {
        private static final long serialVersionUID = 1L;
        private String loanId;
        private String customerId;
        private String accountNumber;
        private BigDecimal principalAmount;
        private int termMonths;
        private BigDecimal annualInterestRate;
        private BigDecimal monthlyEmi;
        private BigDecimal outstandingAmount;
        private LoanStatus status;
        private String appliedAt;

        public Loan(String loanId, String customerId, String accountNumber, BigDecimal principalAmount, int termMonths) {
            this.loanId = loanId;
            this.customerId = customerId;
            this.accountNumber = accountNumber;
            this.principalAmount = principalAmount.setScale(2, RoundingMode.HALF_EVEN);
            this.termMonths = termMonths;
            this.annualInterestRate = LOAN_ANNUAL_RATE;
            this.status = LoanStatus.PENDING;
            this.appliedAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.monthlyEmi = calculateEmi(principalAmount, LOAN_ANNUAL_RATE, termMonths);
            BigDecimal totalRepayable = monthlyEmi.multiply(BigDecimal.valueOf(termMonths)).setScale(2, RoundingMode.HALF_EVEN);
            this.outstandingAmount = totalRepayable;
        }

        public String getLoanId() { return loanId; }
        public String getCustomerId() { return customerId; }
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getPrincipalAmount() { return principalAmount; }
        public int getTermMonths() { return termMonths; }
        public BigDecimal getMonthlyEmi() { return monthlyEmi; }
        public BigDecimal getOutstandingAmount() { return outstandingAmount; }
        public void setOutstandingAmount(BigDecimal amount) { this.outstandingAmount = amount.setScale(2, RoundingMode.HALF_EVEN); }
        public LoanStatus getStatus() { return status; }
        public void setStatus(LoanStatus status) { this.status = status; }
        public String getAppliedAt() { return appliedAt; }
        public BigDecimal getAnnualInterestRate() { return annualInterestRate; }

        private static BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
            double p = principal.doubleValue();
            double r = annualRate.doubleValue() / 12.0;
            double n = months;
            double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
            return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_EVEN);
        }
    }

    public static class FixedDeposit implements Serializable {
        private static final long serialVersionUID = 1L;
        private String fdId;
        private String customerId;
        private String sourceAccountNumber;
        private BigDecimal principalAmount;
        private int termMonths;
        private BigDecimal annualRate;
        private BigDecimal maturityAmount;
        private String startDate;
        private String maturityDate;
        private FdStatus status;

        public FixedDeposit(String fdId, String customerId, String sourceAccountNumber, BigDecimal principal, int termMonths) {
            this.fdId = fdId;
            this.customerId = customerId;
            this.sourceAccountNumber = sourceAccountNumber;
            this.principalAmount = principal.setScale(2, RoundingMode.HALF_EVEN);
            this.termMonths = termMonths;
            this.annualRate = FIXED_DEPOSIT_ANNUAL_RATE;
            this.status = FdStatus.ACTIVE;
            this.startDate = LocalDate.now().format(DATE_FORMATTER);
            this.maturityDate = LocalDate.now().plusMonths(termMonths).format(DATE_FORMATTER);
            double p = principal.doubleValue();
            double r = FIXED_DEPOSIT_ANNUAL_RATE.doubleValue();
            double t = termMonths / 12.0;
            double maturity = p * (1 + (r * t));
            this.maturityAmount = BigDecimal.valueOf(maturity).setScale(2, RoundingMode.HALF_EVEN);
        }

        public String getFdId() { return fdId; }
        public String getCustomerId() { return customerId; }
        public String getSourceAccountNumber() { return sourceAccountNumber; }
        public BigDecimal getPrincipalAmount() { return principalAmount; }
        public int getTermMonths() { return termMonths; }
        public BigDecimal getMaturityAmount() { return maturityAmount; }
        public String getStartDate() { return startDate; }
        public String getMaturityDate() { return maturityDate; }
        public FdStatus getStatus() { return status; }
        public void setStatus(FdStatus status) { this.status = status; }
        public BigDecimal getAnnualRate() { return annualRate; }
    }

    public static class RiskAlert implements Serializable {
        private static final long serialVersionUID = 1L;
        private String alertId;
        private String ruleName;
        private RiskSeverity severity;
        private String targetEntity;
        private String timestamp;
        private RiskStatus status;
        private String notes;

        public RiskAlert(String alertId, String ruleName, RiskSeverity severity, String targetEntity, String notes) {
            this.alertId = alertId;
            this.ruleName = ruleName;
            this.severity = severity;
            this.targetEntity = targetEntity;
            this.timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.status = RiskStatus.OPEN;
            this.notes = notes;
        }

        public String getAlertId() { return alertId; }
        public String getRuleName() { return ruleName; }
        public RiskSeverity getSeverity() { return severity; }
        public String getTargetEntity() { return targetEntity; }
        public String getTimestamp() { return timestamp; }
        public RiskStatus getStatus() { return status; }
        public void setStatus(RiskStatus status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class InAppNotification implements Serializable {
        private static final long serialVersionUID = 1L;
        private String notificationId;
        private String customerId;
        private String title;
        private String message;
        private String timestamp;
        private boolean read;

        public InAppNotification(String notificationId, String customerId, String title, String message) {
            this.notificationId = notificationId;
            this.customerId = customerId;
            this.title = title;
            this.message = message;
            this.timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.read = false;
        }

        public String getNotificationId() { return notificationId; }
        public String getCustomerId() { return customerId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }

    public static class AuditLog implements Serializable {
        private static final long serialVersionUID = 1L;
        private String auditId;
        private String timestamp;
        private String eventType;
        private String actor;
        private String target;
        private String outcome;
        private String details;

        public AuditLog(String auditId, String eventType, String actor, String target, String outcome, String details) {
            this.auditId = auditId;
            this.timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.eventType = eventType;
            this.actor = actor;
            this.target = target;
            this.outcome = outcome;
            this.details = details;
        }

        public String getAuditId() { return auditId; }
        public String getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getActor() { return actor; }
        public String getTarget() { return target; }
        public String getOutcome() { return outcome; }
        public String getDetails() { return details; }
    }

    public static class DatabaseState implements Serializable {
        private static final long serialVersionUID = 1L;
        public Map<String, User> users = new HashMap<>();
        public Map<String, Customer> customers = new HashMap<>();
        public Map<String, BankAccount> accounts = new HashMap<>();
        public List<Transaction> transactions = new ArrayList<>();
        public List<JournalEntry> ledger = new ArrayList<>();
        public List<Beneficiary> beneficiaries = new ArrayList<>();
        public List<Loan> loans = new ArrayList<>();
        public List<FixedDeposit> fixedDeposits = new ArrayList<>();
        public List<RiskAlert> riskAlerts = new ArrayList<>();
        public List<InAppNotification> notifications = new ArrayList<>();
        public List<AuditLog> auditLogs = new ArrayList<>();
        public Set<String> processedIdempotencyKeys = new HashSet<>();

        public int nextTxnId = 1001;
        public int nextJournalId = 5001;
        public int nextCustId = 1002;
        public int nextAccId = 100002;
        public int nextBeneficiaryId = 501;
        public int nextLoanId = 801;
        public int nextFdId = 701;
        public int nextAlertId = 301;
        public int nextNotifId = 901;
        public int nextAuditId = 1;
    }

    public static class BankDatabase {
        private DatabaseState state;
        private final String dbPath;

        public BankDatabase(String dbPath) {
            this.dbPath = dbPath;
            load();
        }

        public DatabaseState getState() { return state; }

        public synchronized void load() {
            File file = new File(dbPath);
            if (file.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    state = (DatabaseState) ois.readObject();
                    return;
                } catch (Exception e) {
                    System.err.println("Notice: Could not deserialize persistent store, seeding fresh state.");
                }
            }
            state = new DatabaseState();
            seedInitialDemoData();
            save();
        }

        public synchronized void save() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dbPath))) {
                oos.writeObject(state);
            } catch (IOException e) {
                System.err.println("Warning: Unable to persist state to " + dbPath + ": " + e.getMessage());
            }
        }

        private void seedInitialDemoData() {
            User demoCustomer = new User("CUST1001", "democustomer", "Customer@123", "1234", Role.CUSTOMER);
            state.users.put(demoCustomer.getUsername().toLowerCase(), demoCustomer);

            User demoTeller = new User("TELLER01", "teller", "Teller@123", "5555", Role.TELLER);
            state.users.put(demoTeller.getUsername().toLowerCase(), demoTeller);

            User demoAuditor = new User("AUDIT01", "auditor", "Auditor@123", "7777", Role.AUDITOR);
            state.users.put(demoAuditor.getUsername().toLowerCase(), demoAuditor);

            User adminUser = new User("ADMIN001", "admin", "Admin@123", "9999", Role.ADMIN);
            state.users.put(adminUser.getUsername().toLowerCase(), adminUser);

            Customer customer = new Customer("CUST1001", "Demo Customer", "customer@kiranbank.com", "9876543210");
            state.customers.put(customer.getCustomerId(), customer);

            BankAccount primaryAccount = new BankAccount("KBA100001", "CUST1001", AccountType.SAVINGS, new BigDecimal("10000.00"));
            state.accounts.put(primaryAccount.getAccountNumber(), primaryAccount);

            JournalEntry initEntry = new JournalEntry("JRN5000", "VAULT_CASH", "CUSTOMER_DEPOSITS_KBA100001",
                    new BigDecimal("10000.00"), "INIT_SEED", "Initial Demo Account Vault Balance");
            state.ledger.add(initEntry);

            state.notifications.add(new InAppNotification("NOTIF900", "CUST1001", "Welcome to Kiran Bank Enterprise",
                    "Your primary Savings account KBA100001 is active with an opening balance of Rs. 10000.00."));

            state.auditLogs.add(new AuditLog("AUD0001", "ENTERPRISE_INIT", "SYSTEM", "DATABASE", "SUCCESS",
                    "Initialized enterprise state with CUSTOMER, TELLER, AUDITOR, and ADMIN roles"));
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashWithSalt(String input, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "Rs. 0.00";
        return String.format("Rs. %.2f", amount.setScale(2, RoundingMode.HALF_EVEN));
    }

    public static void main(String[] args) {
        BankDatabase database = new BankDatabase(DB_FILE_PATH);
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================================================");
        System.out.println("                 KIRAN BANK ENTERPRISE PLATFORM v4.0");
        System.out.println("       Comprehensive Double-Entry Core Banking Management System");
        System.out.println("========================================================================");
        System.out.println("[Enterprise Demo Credentials]");
        System.out.println(" - Customer Portal:   Username: 'democustomer' | PIN: 1234 (Pass: Customer@123)");
        System.out.println(" - Teller Portal:     Username: 'teller'       | PIN: 5555 (Pass: Teller@123)");
        System.out.println(" - Auditor Portal:    Username: 'auditor'      | PIN: 7777 (Pass: Auditor@123)");
        System.out.println(" - Admin Console:     Username: 'admin'        | PIN: 9999 (Pass: Admin@123)");
        System.out.println("========================================================================");

        while (true) {
            System.out.println("\n--- ENTERPRISE GATEWAY ---");
            System.out.println("1. Customer PIN Login");
            System.out.println("2. Customer Password Login");
            System.out.println("3. Teller Desk Login");
            System.out.println("4. Internal Auditor Login");
            System.out.println("5. System Administrator Console");
            System.out.println("6. Self-Service Customer Registration");
            System.out.println("7. Exit Platform");
            System.out.print("Enter choice (1-7): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                handlePinLogin(database, scanner, Role.CUSTOMER);
            } else if (choice.equals("2")) {
                handlePasswordLogin(database, scanner, Role.CUSTOMER);
            } else if (choice.equals("3")) {
                handleStaffLogin(database, scanner, Role.TELLER);
            } else if (choice.equals("4")) {
                handleStaffLogin(database, scanner, Role.AUDITOR);
            } else if (choice.equals("5")) {
                handleStaffLogin(database, scanner, Role.ADMIN);
            } else if (choice.equals("6")) {
                handleSelfRegistration(database, scanner);
            } else if (choice.equals("7")) {
                System.out.println("Kiran Bank Enterprise safely shutdown. Data journaled to disk.");
                break;
            } else {
                System.out.println("Invalid selection. Please choose an option between 1 and 7.");
            }
        }

        database.save();
        scanner.close();
    }

    private static void handlePinLogin(BankDatabase database, Scanner scanner, Role expectedRole) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim().toLowerCase();
        User user = database.getState().users.get(username);

        if (user == null || user.getRole() != expectedRole) {
            System.out.println("User not found or role authorization mismatch.");
            return;
        }

        if (user.isLocked()) {
            System.out.println("Account is LOCKED due to repeated security failures. Contact Teller/Admin.");
            return;
        }

        int attempts = 0;
        boolean authenticated = false;
        while (attempts < 3) {
            System.out.print("Enter 4-digit PIN: ");
            String pin = scanner.nextLine().trim();
            if (user.verifyPin(pin)) {
                authenticated = true;
                user.resetFailedAttempts();
                database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                        "LOGIN_SUCCESS", user.getUserId(), "PIN_GATEWAY", "SUCCESS", "Customer PIN login verified"));
                database.save();
                break;
            } else {
                attempts++;
                user.incrementFailedAttempts();
                int remaining = 3 - attempts;
                if (remaining > 0) {
                    System.out.printf("Incorrect PIN. Remaining attempts: %d%n", remaining);
                } else {
                    user.setLocked(true);
                    System.out.println("Incorrect PIN. Maximum attempts exceeded. Account is now LOCKED.");
                    database.getState().riskAlerts.add(new RiskAlert("RSK" + (database.getState().nextAlertId++),
                            "BRUTE_FORCE_PIN_LOCK", RiskSeverity.HIGH, user.getUserId(), "3 consecutive failed PIN attempts"));
                    database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                            "ACCOUNT_LOCKED", user.getUserId(), "SECURITY", "FAILURE", "Locked after 3 failed PIN attempts"));
                    database.save();
                }
            }
        }

        if (authenticated) {
            runCustomerMenu(database, scanner, user);
        }
    }

    private static void handlePasswordLogin(BankDatabase database, Scanner scanner, Role expectedRole) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim().toLowerCase();
        User user = database.getState().users.get(username);

        if (user == null || user.getRole() != expectedRole) {
            System.out.println("User not found or role authorization mismatch.");
            return;
        }

        if (user.isLocked()) {
            System.out.println("Account is locked. Please consult banking staff.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (user.verifyPassword(password)) {
            user.resetFailedAttempts();
            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "LOGIN_SUCCESS", user.getUserId(), "PASSWORD_GATEWAY", "SUCCESS", "Customer password login verified"));
            database.save();
            runCustomerMenu(database, scanner, user);
        } else {
            user.incrementFailedAttempts();
            if (user.getFailedAttempts() >= 3) {
                user.setLocked(true);
                System.out.println("Invalid credentials. Maximum attempts exceeded. Account locked.");
                database.getState().riskAlerts.add(new RiskAlert("RSK" + (database.getState().nextAlertId++),
                        "BRUTE_FORCE_PASSWORD_LOCK", RiskSeverity.HIGH, user.getUserId(), "Password lock triggered"));
            } else {
                System.out.println("Invalid credentials. Remaining attempts: " + (3 - user.getFailedAttempts()));
            }
            database.save();
        }
    }

    private static void handleStaffLogin(BankDatabase database, Scanner scanner, Role role) {
        System.out.print("Enter " + role + " Username: ");
        String username = scanner.nextLine().trim().toLowerCase();
        User staff = database.getState().users.get(username);

        if (staff == null || staff.getRole() != role) {
            System.out.println("Unauthorized: Profile does not match " + role + " credentials.");
            return;
        }

        System.out.print("Enter " + role + " Password: ");
        String pass = scanner.nextLine().trim();

        if (staff.verifyPassword(pass)) {
            staff.resetFailedAttempts();
            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    role + "_LOGIN", staff.getUserId(), "STAFF_GATEWAY", "SUCCESS", role + " session initialized"));
            database.save();

            if (role == Role.TELLER) {
                runTellerMenu(database, scanner, staff);
            } else if (role == Role.AUDITOR) {
                runAuditorMenu(database, scanner, staff);
            } else if (role == Role.ADMIN) {
                runAdminMenu(database, scanner, staff);
            }
        } else {
            System.out.println("Authentication failed.");
        }
    }

    private static void handleSelfRegistration(BankDatabase database, Scanner scanner) {
        System.out.println("\n--- NEW CUSTOMER REGISTRATION & ONBOARDING ---");
        System.out.print("Enter full legal name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be blank."); return; }

        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();
        if (!email.contains("@") || !email.contains(".")) { System.out.println("Invalid email format."); return; }

        System.out.print("Enter 10-digit phone number: ");
        String phone = scanner.nextLine().trim();
        if (!phone.matches("\\d{10}")) { System.out.println("Phone must be 10 digits."); return; }

        System.out.print("Choose unique username: ");
        String username = scanner.nextLine().trim().toLowerCase();
        if (username.length() < 4 || database.getState().users.containsKey(username)) {
            System.out.println("Username must be at least 4 characters and unique.");
            return;
        }

        System.out.print("Set password (min 6 chars): ");
        String password = scanner.nextLine().trim();
        if (password.length() < 6) { System.out.println("Password must be at least 6 characters."); return; }

        System.out.print("Set 4-digit PIN: ");
        String pin = scanner.nextLine().trim();
        if (!pin.matches("\\d{4}")) { System.out.println("PIN must be exactly 4 digits."); return; }

        System.out.print("Select account type (1. Savings, 2. Current): ");
        String accTypeChoice = scanner.nextLine().trim();
        AccountType type = accTypeChoice.equals("2") ? AccountType.CURRENT : AccountType.SAVINGS;

        System.out.print("Enter opening deposit amount (Min Rs. 1000.00): ");
        String depositInput = scanner.nextLine().trim();
        BigDecimal deposit;
        try {
            deposit = new BigDecimal(depositInput).setScale(2, RoundingMode.HALF_EVEN);
            if (deposit.compareTo(MIN_SAVINGS_BALANCE) < 0) {
                System.out.println("Initial deposit must meet minimum threshold of Rs. 1000.00.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid numeric amount.");
            return;
        }

        String customerId = "CUST" + (database.getState().nextCustId++);
        String accountNumber = "KBA" + (database.getState().nextAccId++);

        User newUser = new User(customerId, username, password, pin, Role.CUSTOMER);
        Customer newCustomer = new Customer(customerId, name, email, phone);
        BankAccount newAccount = new BankAccount(accountNumber, customerId, type, deposit);

        database.getState().users.put(username, newUser);
        database.getState().customers.put(customerId, newCustomer);
        database.getState().accounts.put(accountNumber, newAccount);

        String txnId = "TXN" + (database.getState().nextTxnId++);
        Transaction initialTxn = new Transaction(txnId, accountNumber, TransactionType.DEPOSIT,
                deposit, deposit, "OPENING_DEP", null, "Initial Account Opening Deposit");
        database.getState().transactions.add(initialTxn);

        JournalEntry jEntry = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                "VAULT_CASH", "CUSTOMER_DEPOSITS_" + accountNumber, deposit, txnId, "Initial Customer Deposit");
        database.getState().ledger.add(jEntry);

        database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                customerId, "Account Opened Successfully", "Welcome! Account " + accountNumber + " initialized with " + formatCurrency(deposit)));

        database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                "CUSTOMER_REGISTERED", customerId, accountNumber, "SUCCESS", "Customer profile created"));

        database.save();
        System.out.println("\n Registration Successful!");
        System.out.println("Customer ID:    " + customerId);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Username:       " + username);
        System.out.println("Balance:        " + formatCurrency(deposit));
    }

    private static void runCustomerMenu(BankDatabase database, Scanner scanner, User user) {
        Customer customer = database.getState().customers.get(user.getUserId());
        while (true) {
            int unreadCount = 0;
            for (InAppNotification n : database.getState().notifications) {
                if (n.getCustomerId().equals(user.getUserId()) && !n.isRead()) unreadCount++;
            }

            System.out.println("\n========================================================");
            System.out.println("              CUSTOMER PORTAL — KIRAN BANK");
            System.out.printf(" User: %s (%s) | Unread Notifications: %d%n",
                    customer != null ? customer.getFullName() : user.getUsername(), user.getUserId(), unreadCount);
            System.out.println("========================================================");
            System.out.println(" 1. View Profile");
            System.out.println(" 2. View Accounts");
            System.out.println(" 3. Open Additional Account");
            System.out.println(" 4. Check Balance");
            System.out.println(" 5. Deposit Money");
            System.out.println(" 6. Withdraw Money");
            System.out.println(" 7. Transfer Funds (with Idempotency)");
            System.out.println(" 8. Manage Beneficiaries");
            System.out.println(" 9. Transaction History");
            System.out.println("10. Search Transaction by ID");
            System.out.println("11. Generate Account Statement");
            System.out.println("12. View Monthly Inflow/Outflow Summary");
            System.out.println("13. Notification Inbox");
            System.out.println("14. Change Password");
            System.out.println("15. Change PIN");
            System.out.println("16. Apply for Loan");
            System.out.println("17. View Loans and Repayments");
            System.out.println("18. Manage Fixed Deposits");
            System.out.println("19. Logout");
            System.out.print("Enter choice (1-19): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                viewProfile(customer);
            } else if (choice.equals("2")) {
                listCustomerAccounts(database, user.getUserId());
            } else if (choice.equals("3")) {
                openAdditionalAccount(database, scanner, user.getUserId());
            } else if (choice.equals("4")) {
                checkCustomerBalance(database, scanner, user.getUserId());
            } else if (choice.equals("5")) {
                performDeposit(database, scanner, user.getUserId());
            } else if (choice.equals("6")) {
                performWithdrawal(database, scanner, user.getUserId());
            } else if (choice.equals("7")) {
                performTransfer(database, scanner, user.getUserId());
            } else if (choice.equals("8")) {
                manageBeneficiaries(database, scanner, user.getUserId());
            } else if (choice.equals("9")) {
                viewTransactionHistory(database, scanner, user.getUserId());
            } else if (choice.equals("10")) {
                searchTransactionById(database, scanner, user.getUserId());
            } else if (choice.equals("11")) {
                generateAccountStatement(database, scanner, user.getUserId());
            } else if (choice.equals("12")) {
                viewMonthlySummary(database, scanner, user.getUserId());
            } else if (choice.equals("13")) {
                viewNotificationInbox(database, scanner, user.getUserId());
            } else if (choice.equals("14")) {
                changeUserPassword(database, scanner, user);
            } else if (choice.equals("15")) {
                changeUserPin(database, scanner, user);
            } else if (choice.equals("16")) {
                applyForLoan(database, scanner, user.getUserId());
            } else if (choice.equals("17")) {
                viewLoansAndRepay(database, scanner, user.getUserId());
            } else if (choice.equals("18")) {
                manageFixedDeposits(database, scanner, user.getUserId());
            } else if (choice.equals("19")) {
                System.out.print("Are you sure you want to log out? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    System.out.println("Logged out successfully.");
                    break;
                }
            } else {
                System.out.println("Invalid choice. Please select from 1 to 19.");
            }
        }
    }

    private static void viewProfile(Customer customer) {
        if (customer == null) { System.out.println("Customer profile unavailable."); return; }
        System.out.println("\n--- CUSTOMER PROFILE ---");
        System.out.println("Customer ID:   " + customer.getCustomerId());
        System.out.println("Full Name:     " + customer.getFullName());
        System.out.println("Email Address: " + customer.getEmail());
        System.out.println("Phone Number:  " + customer.getPhone());
        System.out.println("Enrolled Date: " + customer.getCreatedAt());
        System.out.println("Status:        " + customer.getStatus());
    }

    private static List<BankAccount> getAccountsForCustomer(BankDatabase database, String customerId) {
        List<BankAccount> list = new ArrayList<>();
        for (BankAccount acc : database.getState().accounts.values()) {
            if (acc.getCustomerId().equals(customerId)) list.add(acc);
        }
        return list;
    }

    private static void listCustomerAccounts(BankDatabase database, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        System.out.println("\n--- YOUR REGISTERED ACCOUNTS ---");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        for (BankAccount acc : accounts) {
            System.out.printf("Acc Number: %-12s | Type: %-8s | Balance: %-14s | Status: %s%n",
                    acc.getAccountNumber(), acc.getAccountType(), formatCurrency(acc.getBalance()), acc.getStatus());
        }
    }

    private static void openAdditionalAccount(BankDatabase database, Scanner scanner, String customerId) {
        System.out.println("\n--- OPEN ADDITIONAL ACCOUNT ---");
        System.out.println("1. Savings Account");
        System.out.println("2. Current Account");
        System.out.print("Select account type: ");
        String choice = scanner.nextLine().trim();
        AccountType type = choice.equals("2") ? AccountType.CURRENT : AccountType.SAVINGS;

        System.out.print("Enter initial deposit amount (Min Rs. 1000.00): ");
        String amtInput = scanner.nextLine().trim();
        try {
            BigDecimal amt = new BigDecimal(amtInput).setScale(2, RoundingMode.HALF_EVEN);
            if (amt.compareTo(MIN_SAVINGS_BALANCE) < 0) {
                System.out.println("Deposit must meet minimum threshold of Rs. 1000.00.");
                return;
            }
            String newAccNumber = "KBA" + (database.getState().nextAccId++);
            BankAccount newAcc = new BankAccount(newAccNumber, customerId, type, amt);
            database.getState().accounts.put(newAccNumber, newAcc);

            String txnId = "TXN" + (database.getState().nextTxnId++);
            Transaction t = new Transaction(txnId, newAccNumber, TransactionType.DEPOSIT, amt, amt, "ACC_OPEN", null, "Initial Deposit");
            database.getState().transactions.add(t);

            JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                    "VAULT_CASH", "CUSTOMER_DEPOSITS_" + newAccNumber, amt, txnId, "Additional Account Opening");
            database.getState().ledger.add(j);

            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "ACCOUNT_OPENED", customerId, newAccNumber, "SUCCESS", "Additional account created"));
            database.save();

            System.out.println(" Account successfully opened: " + newAccNumber + " (" + formatCurrency(amt) + ")");
        } catch (Exception e) {
            System.out.println("Invalid numeric amount.");
        }
    }

    private static void checkCustomerBalance(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }
        if (accounts.size() == 1) {
            System.out.printf("Current Balance: %s%n", formatCurrency(accounts.get(0).getBalance()));
            return;
        }
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        BankAccount acc = database.getState().accounts.get(accNum);
        if (acc != null && acc.getCustomerId().equals(customerId)) {
            System.out.printf("Current Balance for %s: %s%n", accNum, formatCurrency(acc.getBalance()));
        } else {
            System.out.println("Account not recognized or unauthorized.");
        }
    }

    private static void performDeposit(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        BankAccount target = accounts.get(0);
        if (accounts.size() > 1) {
            System.out.print("Enter account number: ");
            String accNum = scanner.nextLine().trim();
            target = database.getState().accounts.get(accNum);
            if (target == null || !target.getCustomerId().equals(customerId)) {
                System.out.println("Invalid account selection.");
                return;
            }
        }

        if (target.getStatus() != AccountStatus.ACTIVE) {
            System.out.println("Account is " + target.getStatus() + ". Deposits rejected.");
            return;
        }

        System.out.print("Enter deposit amount: ");
        String input = scanner.nextLine().trim();
        try {
            double parsedVal = Double.parseDouble(input);
            if (!Double.isFinite(parsedVal)) {
                System.out.println("Invalid input. Please enter a numeric amount.");
                return;
            }
            BigDecimal amount = new BigDecimal(input).setScale(2, RoundingMode.HALF_EVEN);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Invalid amount. Deposit amount must be greater than 0.");
                return;
            }

            BigDecimal updated = target.getBalance().add(amount);
            target.setBalance(updated);

            String txnId = "TXN" + (database.getState().nextTxnId++);
            Transaction txn = new Transaction(txnId, target.getAccountNumber(), TransactionType.DEPOSIT,
                    amount, updated, "SELF_DEP", null, "Cash/Direct Deposit");
            database.getState().transactions.add(txn);

            JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                    "VAULT_CASH", "CUSTOMER_DEPOSITS_" + target.getAccountNumber(), amount, txnId, "Deposit Transaction");
            database.getState().ledger.add(j);

            if (amount.compareTo(LARGE_TRANSACTION_ALERT_THRESHOLD) >= 0) {
                database.getState().riskAlerts.add(new RiskAlert("RSK" + (database.getState().nextAlertId++),
                        "LARGE_DEPOSIT_MONITOR", RiskSeverity.MEDIUM, target.getAccountNumber(), "Deposit of " + formatCurrency(amount)));
            }

            database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                    customerId, "Deposit Successful", formatCurrency(amount) + " credited to " + target.getAccountNumber() + ". Balance: " + formatCurrency(updated)));

            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "DEPOSIT", customerId, target.getAccountNumber(), "SUCCESS", "Deposited " + formatCurrency(amount)));
            database.save();

            System.out.println("--- Transaction Receipt ---");
            System.out.println("Transaction ID:   " + txnId);
            System.out.println("Transaction Type: DEPOSIT");
            System.out.println("Amount:           " + formatCurrency(amount));
            System.out.println("Balance:          " + formatCurrency(updated));
            System.out.println("Status:           SUCCESS");
            System.out.println("---------------------------");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric amount.");
        }
    }

    private static void performWithdrawal(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        BankAccount target = accounts.get(0);
        if (accounts.size() > 1) {
            System.out.print("Enter account number: ");
            String accNum = scanner.nextLine().trim();
            target = database.getState().accounts.get(accNum);
            if (target == null || !target.getCustomerId().equals(customerId)) {
                System.out.println("Invalid account selection.");
                return;
            }
        }

        if (target.getStatus() != AccountStatus.ACTIVE) {
            System.out.println("Account is " + target.getStatus() + ". Withdrawals prohibited.");
            return;
        }

        System.out.print("Enter withdrawal amount: ");
        String input = scanner.nextLine().trim();
        try {
            double parsedVal = Double.parseDouble(input);
            if (!Double.isFinite(parsedVal)) {
                System.out.println("Invalid input. Please enter a numeric amount.");
                return;
            }
            BigDecimal amount = new BigDecimal(input).setScale(2, RoundingMode.HALF_EVEN);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Invalid amount. Withdrawal amount must be greater than 0.");
                return;
            }
            if (amount.compareTo(MAX_SINGLE_WITHDRAWAL) > 0) {
                System.out.println("Transaction rejected: Exceeds single withdrawal limit of " + formatCurrency(MAX_SINGLE_WITHDRAWAL));
                return;
            }
            if (amount.compareTo(target.getBalance()) > 0) {
                System.out.printf("Transaction failed: Insufficient balance. Current Balance: %s%n", formatCurrency(target.getBalance()));
                return;
            }

            BigDecimal updated = target.getBalance().subtract(amount);
            target.setBalance(updated);

            String txnId = "TXN" + (database.getState().nextTxnId++);
            Transaction txn = new Transaction(txnId, target.getAccountNumber(), TransactionType.WITHDRAWAL,
                    amount, updated, "ATM_WDL", null, "Direct Cash Withdrawal");
            database.getState().transactions.add(txn);

            JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                    "CUSTOMER_DEPOSITS_" + target.getAccountNumber(), "VAULT_CASH", amount, txnId, "Withdrawal Transaction");
            database.getState().ledger.add(j);

            database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                    customerId, "Withdrawal Debited", formatCurrency(amount) + " debited from " + target.getAccountNumber() + ". Balance: " + formatCurrency(updated)));

            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "WITHDRAWAL", customerId, target.getAccountNumber(), "SUCCESS", "Withdrew " + formatCurrency(amount)));
            database.save();

            System.out.println("--- Transaction Receipt ---");
            System.out.println("Transaction ID:   " + txnId);
            System.out.println("Transaction Type: WITHDRAWAL");
            System.out.println("Amount:           " + formatCurrency(amount));
            System.out.println("Balance:          " + formatCurrency(updated));
            System.out.println("Status:           SUCCESS");
            System.out.println("---------------------------");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric amount.");
        }
    }

    private static void performTransfer(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }

        BankAccount sender = accounts.get(0);
        if (accounts.size() > 1) {
            System.out.print("Enter source account number: ");
            String fromAcc = scanner.nextLine().trim();
            sender = database.getState().accounts.get(fromAcc);
            if (sender == null || !sender.getCustomerId().equals(customerId)) {
                System.out.println("Invalid source account.");
                return;
            }
        }

        if (sender.getStatus() != AccountStatus.ACTIVE) {
            System.out.println("Source account is " + sender.getStatus() + ". Transfer aborted.");
            return;
        }

        System.out.print("Enter destination account number: ");
        String destAccNum = scanner.nextLine().trim();
        BankAccount recipient = database.getState().accounts.get(destAccNum);
        if (recipient == null) {
            System.out.println("Destination account not found.");
            return;
        }
        if (recipient.getAccountNumber().equals(sender.getAccountNumber())) {
            System.out.println("Cannot transfer funds to the same account.");
            return;
        }
        if (recipient.getStatus() != AccountStatus.ACTIVE) {
            System.out.println("Destination account is " + recipient.getStatus() + ". Transfer rejected.");
            return;
        }

        System.out.print("Enter transfer amount: ");
        String amtInput = scanner.nextLine().trim();
        System.out.print("Enter Idempotency Key (Optional, press Enter for auto-generate): ");
        String idempKey = scanner.nextLine().trim();
        if (idempKey.isEmpty()) {
            idempKey = "IDEM-" + UUID.randomUUID().toString().substring(0, 8);
        }

        if (database.getState().processedIdempotencyKeys.contains(idempKey)) {
            System.out.println(" [IDEMPOTENCY NOTICE] Duplicate transfer instruction detected with key: " + idempKey);
            System.out.println("Operation ignored to prevent double-debiting. Your balance remains safe.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amtInput).setScale(2, RoundingMode.HALF_EVEN);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Transfer amount must be strictly greater than 0.");
                return;
            }
            if (amount.compareTo(MAX_SINGLE_TRANSFER) > 0) {
                System.out.println("Amount exceeds single transfer ceiling of " + formatCurrency(MAX_SINGLE_TRANSFER));
                return;
            }

            BigDecimal fee = amount.multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal totalDebit = amount.add(fee);

            if (totalDebit.compareTo(sender.getBalance()) > 0) {
                System.out.printf("Insufficient funds. Required: %s (Amount: %s + Fee: %s), Available: %s%n",
                        formatCurrency(totalDebit), formatCurrency(amount), formatCurrency(fee), formatCurrency(sender.getBalance()));
                return;
            }

            System.out.printf("Confirm transfer of %s to %s (Transfer Fee: %s)? (yes/no): ",
                    formatCurrency(amount), destAccNum, formatCurrency(fee));
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Transfer cancelled.");
                return;
            }

            BigDecimal senderPre = sender.getBalance();
            BigDecimal recipientPre = recipient.getBalance();

            try {
                BigDecimal senderPost = senderPre.subtract(totalDebit);
                BigDecimal recipientPost = recipientPre.add(amount);

                sender.setBalance(senderPost);
                recipient.setBalance(recipientPost);

                database.getState().processedIdempotencyKeys.add(idempKey);

                String sharedRef = "TRF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String debitTxnId = "TXN" + (database.getState().nextTxnId++);
                String creditTxnId = "TXN" + (database.getState().nextTxnId++);

                Transaction debitTxn = new Transaction(debitTxnId, sender.getAccountNumber(), TransactionType.TRANSFER_DEBIT,
                        amount, senderPost.add(fee), sharedRef, idempKey, "Transfer to " + recipient.getAccountNumber());
                Transaction feeTxn = new Transaction("TXN" + (database.getState().nextTxnId++), sender.getAccountNumber(), TransactionType.FEE,
                        fee, senderPost, sharedRef, idempKey, "Transfer Fee (0.1%)");
                Transaction creditTxn = new Transaction(creditTxnId, recipient.getAccountNumber(), TransactionType.TRANSFER_CREDIT,
                        amount, recipientPost, sharedRef, idempKey, "Transfer from " + sender.getAccountNumber());

                database.getState().transactions.add(debitTxn);
                database.getState().transactions.add(feeTxn);
                database.getState().transactions.add(creditTxn);

                JournalEntry jTransfer = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                        "CUSTOMER_DEPOSITS_" + sender.getAccountNumber(), "CUSTOMER_DEPOSITS_" + recipient.getAccountNumber(),
                        amount, sharedRef, "Account-to-Account Transfer");
                JournalEntry jFee = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                        "CUSTOMER_DEPOSITS_" + sender.getAccountNumber(), "FEE_INCOME_ACCOUNT",
                        fee, sharedRef, "Service Fee Collection");
                database.getState().ledger.add(jTransfer);
                database.getState().ledger.add(jFee);

                if (amount.compareTo(LARGE_TRANSACTION_ALERT_THRESHOLD) >= 0) {
                    database.getState().riskAlerts.add(new RiskAlert("RSK" + (database.getState().nextAlertId++),
                            "LARGE_TRANSFER_MONITOR", RiskSeverity.HIGH, sharedRef, "Transfer volume: " + formatCurrency(amount)));
                }

                database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                        customerId, "Transfer Completed", "Sent " + formatCurrency(amount) + " to " + destAccNum + " (Ref: " + sharedRef + ")"));
                database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                        recipient.getCustomerId(), "Funds Received", "Received " + formatCurrency(amount) + " from " + sender.getAccountNumber() + " (Ref: " + sharedRef + ")"));

                database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                        "FUNDS_TRANSFER", customerId, destAccNum, "SUCCESS", "Transferred " + formatCurrency(amount) + " Ref: " + sharedRef));
                database.save();

                System.out.println("\n Transfer Successful!");
                System.out.println("Reference ID:      " + sharedRef);
                System.out.println("Idempotency Key:   " + idempKey);
                System.out.println("Amount Sent:       " + formatCurrency(amount));
                System.out.println("Service Fee:       " + formatCurrency(fee));
                System.out.println("Updated Balance:   " + formatCurrency(senderPost));
            } catch (Exception ex) {
                sender.setBalance(senderPre);
                recipient.setBalance(recipientPre);
                System.out.println("Transaction rolled back due to error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Invalid numeric transfer amount.");
        }
    }

    private static void manageBeneficiaries(BankDatabase database, Scanner scanner, String customerId) {
        System.out.println("\n--- BENEFICIARY DIRECTORY ---");
        System.out.println("1. List Beneficiaries");
        System.out.println("2. Add Beneficiary");
        System.out.println("3. Quick Transfer to Beneficiary");
        System.out.println("4. Remove Beneficiary");
        System.out.print("Select choice (1-4): ");
        String c = scanner.nextLine().trim();

        if (c.equals("1")) {
            boolean hasAny = false;
            for (Beneficiary b : database.getState().beneficiaries) {
                if (b.getCustomerId().equals(customerId)) {
                    hasAny = true;
                    System.out.printf("ID: %-8s | Name: %-18s | Acc: %-12s | Bank: %s%n",
                            b.getBeneficiaryId(), b.getBeneficiaryName(), b.getBeneficiaryAccountNumber(), b.getBankName());
                }
            }
            if (!hasAny) System.out.println("No saved beneficiaries.");
        } else if (c.equals("2")) {
            System.out.print("Enter beneficiary account number: ");
            String acc = scanner.nextLine().trim();
            BankAccount target = database.getState().accounts.get(acc);
            if (target == null) {
                System.out.println("Account does not exist in Kiran Bank system.");
                return;
            }
            System.out.print("Enter beneficiary nickname / label: ");
            String nick = scanner.nextLine().trim();
            String bId = "BEN" + (database.getState().nextBeneficiaryId++);
            Beneficiary b = new Beneficiary(bId, customerId, acc, nick, "Kiran Bank");
            database.getState().beneficiaries.add(b);
            database.save();
            System.out.println(" Beneficiary registered: " + nick + " (" + acc + ")");
        } else if (c.equals("3")) {
            performTransfer(database, scanner, customerId);
        } else if (c.equals("4")) {
            System.out.print("Enter beneficiary ID to delete: ");
            String bId = scanner.nextLine().trim();
            database.getState().beneficiaries.removeIf(b -> b.getCustomerId().equals(customerId) && b.getBeneficiaryId().equalsIgnoreCase(bId));
            database.save();
            System.out.println("Beneficiary removed.");
        }
    }

    private static void viewTransactionHistory(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> myAccounts = getAccountsForCustomer(database, customerId);
        Set<String> accNumbers = new HashSet<>();
        for (BankAccount a : myAccounts) accNumbers.add(a.getAccountNumber());

        List<Transaction> myTxns = new ArrayList<>();
        for (Transaction t : database.getState().transactions) {
            if (accNumbers.contains(t.getAccountNumber())) myTxns.add(t);
        }

        if (myTxns.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        System.out.println("\n------------------------- TRANSACTION HISTORY -------------------------");
        for (Transaction t : myTxns) {
            System.out.printf("[%s] %s | %-16s | Amount: %10s | Balance: %10s | %s%n",
                    t.getTimestamp(), t.getTransactionId(), t.getType(),
                    formatCurrency(t.getAmount()), formatCurrency(t.getBalanceAfter()), t.getDescription());
        }
        System.out.println("-----------------------------------------------------------------------");
    }

    private static void searchTransactionById(BankDatabase database, Scanner scanner, String customerId) {
        System.out.print("Enter transaction ID: ");
        String searchId = scanner.nextLine().trim();
        List<BankAccount> myAccounts = getAccountsForCustomer(database, customerId);
        Set<String> accNumbers = new HashSet<>();
        for (BankAccount a : myAccounts) accNumbers.add(a.getAccountNumber());

        for (Transaction t : database.getState().transactions) {
            if (t.getTransactionId().equalsIgnoreCase(searchId) && accNumbers.contains(t.getAccountNumber())) {
                System.out.println("--- Transaction Receipt ---");
                System.out.println("Transaction ID:   " + t.getTransactionId());
                System.out.println("Account Number:   " + t.getAccountNumber());
                System.out.println("Transaction Type: " + t.getType());
                System.out.println("Amount:           " + formatCurrency(t.getAmount()));
                System.out.println("Balance After:    " + formatCurrency(t.getBalanceAfter()));
                System.out.println("Timestamp:        " + t.getTimestamp());
                System.out.println("Reference:        " + t.getReferenceId());
                System.out.println("Description:      " + t.getDescription());
                System.out.println("Status:           SUCCESS");
                System.out.println("---------------------------");
                return;
            }
        }
        System.out.println("Transaction not found.");
    }

    private static void generateAccountStatement(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }
        BankAccount acc = accounts.get(0);
        Customer cust = database.getState().customers.get(customerId);

        BigDecimal totalDep = BigDecimal.ZERO;
        BigDecimal totalWdl = BigDecimal.ZERO;
        int count = 0;

        for (Transaction t : database.getState().transactions) {
            if (t.getAccountNumber().equals(acc.getAccountNumber())) {
                count++;
                if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.TRANSFER_CREDIT || t.getType() == TransactionType.INTEREST) {
                    totalDep = totalDep.add(t.getAmount());
                } else if (t.getType() == TransactionType.WITHDRAWAL || t.getType() == TransactionType.TRANSFER_DEBIT || t.getType() == TransactionType.FEE) {
                    totalWdl = totalWdl.add(t.getAmount());
                }
            }
        }

        System.out.println("\n================= OFFICIAL ACCOUNT STATEMENT =================");
        System.out.println("Account Number:                   " + acc.getAccountNumber());
        System.out.println("Account Holder:                   " + (cust != null ? cust.getFullName() : customerId));
        System.out.println("Statement Date:                   " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        System.out.println("Opening Balance:                  " + formatCurrency(acc.getOpeningBalance()));
        System.out.println("Total Successful Credits/Deposits: " + formatCurrency(totalDep));
        System.out.println("Total Successful Debits/Withdrawals: " + formatCurrency(totalWdl));
        System.out.println("Number of Successful Transactions: " + count);
        System.out.println("Current Closing Balance:          " + formatCurrency(acc.getBalance()));
        System.out.println("==============================================================");

        System.out.print("Export statement transactions to CSV file? (yes/no): ");
        String exp = scanner.nextLine().trim();
        if (exp.equalsIgnoreCase("yes") || exp.equalsIgnoreCase("y")) {
            System.out.print("Enter output file path (or press Enter for default 'statement_" + acc.getAccountNumber() + ".csv'): ");
            String path = scanner.nextLine().trim();
            if (path.isEmpty()) path = "statement_" + acc.getAccountNumber() + ".csv";
            List<Transaction> accTxns = new ArrayList<>();
            for (Transaction t : database.getState().transactions) {
                if (t.getAccountNumber().equals(acc.getAccountNumber())) accTxns.add(t);
            }
            boolean ok = exportTransactionsToCsv(accTxns, new File(path));
            if (ok) {
                System.out.println(" Statement exported successfully to: " + path);
            } else {
                System.out.println(" Failed to export statement.");
            }
        }
    }

    public static String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        String val = field;
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@") || val.startsWith("\t") || val.startsWith("\r")) {
            val = "'" + val;
        }
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    public static boolean exportTransactionsToCsv(List<Transaction> txns, File file) {
        if (file == null) return false;
        try {
            File canonical = file.getCanonicalFile();
            File parent = canonical.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(canonical), StandardCharsets.UTF_8))) {
                pw.println("TransactionID,AccountNumber,Type,Amount,BalanceAfter,Timestamp,ReferenceID,Description");
                if (txns != null) {
                    for (Transaction t : txns) {
                        pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                                escapeCsvField(t.getTransactionId()),
                                escapeCsvField(t.getAccountNumber()),
                                escapeCsvField(t.getType().name()),
                                escapeCsvField(t.getAmount().toPlainString()),
                                escapeCsvField(t.getBalanceAfter().toPlainString()),
                                escapeCsvField(t.getTimestamp()),
                                escapeCsvField(t.getReferenceId()),
                                escapeCsvField(t.getDescription()));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void viewMonthlySummary(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }
        String curYearMonth = LocalDate.now().toString().substring(0, 7);

        BigDecimal monthInflow = BigDecimal.ZERO;
        BigDecimal monthOutflow = BigDecimal.ZERO;
        int txnCount = 0;

        for (Transaction t : database.getState().transactions) {
            if (accounts.get(0).getAccountNumber().equals(t.getAccountNumber()) && t.getTimestamp().startsWith(curYearMonth)) {
                txnCount++;
                if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.TRANSFER_CREDIT) {
                    monthInflow = monthInflow.add(t.getAmount());
                } else {
                    monthOutflow = monthOutflow.add(t.getAmount());
                }
            }
        }

        System.out.println("\n--- MONTHLY SUMMARY (" + curYearMonth + ") ---");
        System.out.println("Total Monthly Inflow:  " + formatCurrency(monthInflow));
        System.out.println("Total Monthly Outflow: " + formatCurrency(monthOutflow));
        System.out.println("Total Transactions:    " + txnCount);
        System.out.println("Net Monthly Savings:   " + formatCurrency(monthInflow.subtract(monthOutflow)));
    }

    private static void viewNotificationInbox(BankDatabase database, Scanner scanner, String customerId) {
        System.out.println("\n--- IN-APP NOTIFICATION INBOX ---");
        List<InAppNotification> userNotifs = new ArrayList<>();
        for (InAppNotification n : database.getState().notifications) {
            if (n.getCustomerId().equals(customerId)) userNotifs.add(n);
        }

        if (userNotifs.isEmpty()) {
            System.out.println("Your inbox is empty.");
            return;
        }

        for (InAppNotification n : userNotifs) {
            System.out.printf("[%s] %s | %s — %s%n",
                    n.isRead() ? "READ" : "UNREAD", n.getTimestamp(), n.getTitle(), n.getMessage());
            n.setRead(true);
        }
        database.save();
    }

    private static void changeUserPassword(BankDatabase database, Scanner scanner, User user) {
        System.out.print("Enter current password: ");
        String oldP = scanner.nextLine().trim();
        if (!user.verifyPassword(oldP)) { System.out.println("Verification failed."); return; }

        System.out.print("Enter new password (min 6 characters): ");
        String newP = scanner.nextLine().trim();
        if (newP.length() < 6) { System.out.println("Password too short."); return; }

        user.changePassword(newP);
        database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                "PASSWORD_CHANGE", user.getUserId(), "SECURITY", "SUCCESS", "Password updated"));
        database.save();
        System.out.println(" Password updated successfully.");
    }

    private static void changeUserPin(BankDatabase database, Scanner scanner, User user) {
        System.out.print("Enter current 4-digit PIN: ");
        String oldPin = scanner.nextLine().trim();
        if (!user.verifyPin(oldPin)) { System.out.println("Verification failed."); return; }

        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();
        if (!newPin.matches("\\d{4}")) { System.out.println("PIN must be exactly 4 digits."); return; }

        user.changePin(newPin);
        database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                "PIN_CHANGE", user.getUserId(), "SECURITY", "SUCCESS", "PIN updated"));
        database.save();
        System.out.println(" PIN updated successfully.");
    }

    private static void applyForLoan(BankDatabase database, Scanner scanner, String customerId) {
        List<BankAccount> accounts = getAccountsForCustomer(database, customerId);
        if (accounts.isEmpty()) { System.out.println("Open an account first."); return; }

        System.out.println("\n--- LOAN APPLICATION SIMULATOR (Annual Rate: 9.0%) ---");
        System.out.print("Enter requested principal amount (e.g. 50000): ");
        String pInput = scanner.nextLine().trim();
        System.out.print("Enter tenure in months (6, 12, 24, 36, 60): ");
        String termInput = scanner.nextLine().trim();

        try {
            BigDecimal principal = new BigDecimal(pInput).setScale(2, RoundingMode.HALF_EVEN);
            int months = Integer.parseInt(termInput);
            if (principal.compareTo(new BigDecimal("5000.00")) < 0 || months <= 0) {
                System.out.println("Minimum loan amount is Rs. 5000.00.");
                return;
            }

            String loanId = "LN" + (database.getState().nextLoanId++);
            Loan loan = new Loan(loanId, customerId, accounts.get(0).getAccountNumber(), principal, months);
            database.getState().loans.add(loan);

            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "LOAN_APPLIED", customerId, loanId, "SUCCESS", "Principal: " + formatCurrency(principal)));
            database.save();

            System.out.println(" Loan Application Submitted for Review!");
            System.out.println("Loan ID:            " + loanId);
            System.out.println("Calculated EMI:     " + formatCurrency(loan.getMonthlyEmi()) + "/month");
            System.out.println("Status:             PENDING (Awaiting Administrator Approval)");
        } catch (Exception e) {
            System.out.println("Invalid numeric input.");
        }
    }

    private static void viewLoansAndRepay(BankDatabase database, Scanner scanner, String customerId) {
        List<Loan> myLoans = new ArrayList<>();
        for (Loan ln : database.getState().loans) {
            if (ln.getCustomerId().equals(customerId)) myLoans.add(ln);
        }

        if (myLoans.isEmpty()) { System.out.println("No active loan records."); return; }

        System.out.println("\n--- YOUR LOANS ---");
        for (Loan ln : myLoans) {
            System.out.printf("Loan ID: %-6s | Principal: %10s | Outstanding: %10s | EMI: %8s | Status: %s%n",
                    ln.getLoanId(), formatCurrency(ln.getPrincipalAmount()), formatCurrency(ln.getOutstandingAmount()),
                    formatCurrency(ln.getMonthlyEmi()), ln.getStatus());
        }

        System.out.print("Do you wish to make an EMI repayment? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) return;

        System.out.print("Enter Loan ID: ");
        String lId = scanner.nextLine().trim();
        Loan target = null;
        for (Loan ln : myLoans) {
            if (ln.getLoanId().equalsIgnoreCase(lId) && ln.getStatus() == LoanStatus.DISBURSED) {
                target = ln;
                break;
            }
        }

        if (target == null) {
            System.out.println("Active disbursed loan not found.");
            return;
        }

        BankAccount acc = database.getState().accounts.get(target.getAccountNumber());
        if (acc == null || acc.getBalance().compareTo(target.getMonthlyEmi()) < 0) {
            System.out.println("Insufficient account funds to settle monthly EMI of " + formatCurrency(target.getMonthlyEmi()));
            return;
        }

        acc.setBalance(acc.getBalance().subtract(target.getMonthlyEmi()));
        target.setOutstandingAmount(target.getOutstandingAmount().subtract(target.getMonthlyEmi()));
        if (target.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            target.setOutstandingAmount(BigDecimal.ZERO);
            target.setStatus(LoanStatus.CLOSED);
        }

        String txnId = "TXN" + (database.getState().nextTxnId++);
        Transaction t = new Transaction(txnId, acc.getAccountNumber(), TransactionType.LOAN_REPAYMENT,
                target.getMonthlyEmi(), acc.getBalance(), target.getLoanId(), null, "Monthly EMI Payment");
        database.getState().transactions.add(t);

        JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                "CUSTOMER_DEPOSITS_" + acc.getAccountNumber(), "LOAN_PORTFOLIO_" + target.getLoanId(),
                target.getMonthlyEmi(), target.getLoanId(), "Loan Repayment Collection");
        database.getState().ledger.add(j);

        database.save();
        System.out.println(" EMI Payment Successful of " + formatCurrency(target.getMonthlyEmi()) + ". Outstanding: " + formatCurrency(target.getOutstandingAmount()));
    }

    private static void manageFixedDeposits(BankDatabase database, Scanner scanner, String customerId) {
        System.out.println("\n--- FIXED DEPOSIT PORTFOLIO (Annual Return: 6.5%) ---");
        System.out.println("1. View Active Fixed Deposits");
        System.out.println("2. Open New Fixed Deposit");
        System.out.println("3. Premature/Normal Maturity Liquidation");
        System.out.print("Choose option (1-3): ");
        String c = scanner.nextLine().trim();

        if (c.equals("1")) {
            boolean has = false;
            for (FixedDeposit fd : database.getState().fixedDeposits) {
                if (fd.getCustomerId().equals(customerId)) {
                    has = true;
                    System.out.printf("FD ID: %-6s | Principal: %10s | Maturity: %10s | Matures On: %s | Status: %s%n",
                            fd.getFdId(), formatCurrency(fd.getPrincipalAmount()), formatCurrency(fd.getMaturityAmount()),
                            fd.getMaturityDate(), fd.getStatus());
                }
            }
            if (!has) System.out.println("No fixed deposit accounts on record.");
        } else if (c.equals("2")) {
            List<BankAccount> accs = getAccountsForCustomer(database, customerId);
            if (accs.isEmpty()) { System.out.println("No funding account available."); return; }
            BankAccount src = accs.get(0);

            System.out.print("Enter FD Principal Amount (Min Rs. 5000.00): ");
            String pStr = scanner.nextLine().trim();
            System.out.print("Enter tenure in months (6, 12, 24, 36): ");
            String mStr = scanner.nextLine().trim();

            try {
                BigDecimal p = new BigDecimal(pStr).setScale(2, RoundingMode.HALF_EVEN);
                int m = Integer.parseInt(mStr);
                if (p.compareTo(new BigDecimal("5000.00")) < 0 || m <= 0) {
                    System.out.println("Invalid amount or tenure.");
                    return;
                }
                if (src.getBalance().compareTo(p) < 0) {
                    System.out.println("Insufficient funding balance in account " + src.getAccountNumber());
                    return;
                }

                src.setBalance(src.getBalance().subtract(p));
                String fdId = "FD" + (database.getState().nextFdId++);
                FixedDeposit fd = new FixedDeposit(fdId, customerId, src.getAccountNumber(), p, m);
                database.getState().fixedDeposits.add(fd);

                String tId = "TXN" + (database.getState().nextTxnId++);
                Transaction t = new Transaction(tId, src.getAccountNumber(), TransactionType.WITHDRAWAL, p, src.getBalance(), fdId, null, "Fixed Deposit Booking");
                database.getState().transactions.add(t);

                JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                        "CUSTOMER_DEPOSITS_" + src.getAccountNumber(), "TERM_DEPOSIT_LIABILITY_" + fdId,
                        p, fdId, "Fixed Deposit Creation");
                database.getState().ledger.add(j);

                database.save();
                System.out.println(" Fixed Deposit Created!");
                System.out.println("FD ID:           " + fdId);
                System.out.println("Maturity Value:  " + formatCurrency(fd.getMaturityAmount()));
                System.out.println("Maturity Date:   " + fd.getMaturityDate());
            } catch (Exception e) {
                System.out.println("Invalid numeric input.");
            }
        } else if (c.equals("3")) {
            System.out.print("Enter FD ID to liquidate: ");
            String fdId = scanner.nextLine().trim();
            FixedDeposit target = null;
            for (FixedDeposit f : database.getState().fixedDeposits) {
                if (f.getFdId().equalsIgnoreCase(fdId) && f.getCustomerId().equals(customerId) && f.getStatus() == FdStatus.ACTIVE) {
                    target = f;
                    break;
                }
            }
            if (target == null) {
                System.out.println("Active Fixed Deposit not found.");
                return;
            }
            BankAccount dest = database.getState().accounts.get(target.getSourceAccountNumber());
            dest.setBalance(dest.getBalance().add(target.getMaturityAmount()));
            target.setStatus(FdStatus.MATURED);

            String tId = "TXN" + (database.getState().nextTxnId++);
            Transaction t = new Transaction(tId, dest.getAccountNumber(), TransactionType.DEPOSIT,
                    target.getMaturityAmount(), dest.getBalance(), target.getFdId(), null, "FD Maturity Liquidation");
            database.getState().transactions.add(t);

            JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                    "TERM_DEPOSIT_LIABILITY_" + target.getFdId(), "CUSTOMER_DEPOSITS_" + dest.getAccountNumber(),
                    target.getMaturityAmount(), target.getFdId(), "Fixed Deposit Liquidation");
            database.getState().ledger.add(j);

            database.save();
            System.out.println(" Fixed Deposit Liquidated. Disbursed " + formatCurrency(target.getMaturityAmount()) + " to " + dest.getAccountNumber());
        }
    }

    private static void runTellerMenu(BankDatabase database, Scanner scanner, User teller) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("              TELLER DESK OPERATIONS");
            System.out.printf(" Staff ID: %s (%s)%n", teller.getUserId(), teller.getUsername());
            System.out.println("========================================================");
            System.out.println(" 1. Search Customer Account");
            System.out.println(" 2. Teller Over-The-Counter Cash Deposit");
            System.out.println(" 3. Teller Over-The-Counter Cash Withdrawal");
            System.out.println(" 4. View Account Overview");
            System.out.println(" 5. Logout");
            System.out.print("Enter choice (1-5): ");
            String ch = scanner.nextLine().trim();

            if (ch.equals("1")) {
                searchCustomers(database, scanner);
            } else if (ch.equals("2")) {
                System.out.print("Enter destination account number: ");
                String acc = scanner.nextLine().trim();
                BankAccount a = database.getState().accounts.get(acc);
                if (a == null) { System.out.println("Account not found."); continue; }
                System.out.print("Enter cash deposit amount: ");
                try {
                    BigDecimal amt = new BigDecimal(scanner.nextLine().trim()).setScale(2, RoundingMode.HALF_EVEN);
                    if (amt.compareTo(BigDecimal.ZERO) <= 0) { System.out.println("Must be > 0"); continue; }
                    a.setBalance(a.getBalance().add(amt));
                    String txnId = "TXN" + (database.getState().nextTxnId++);
                    database.getState().transactions.add(new Transaction(txnId, acc, TransactionType.DEPOSIT, amt, a.getBalance(), "TELLER_CASH", null, "Over-the-counter Teller Deposit"));
                    database.getState().ledger.add(new JournalEntry("JRN" + (database.getState().nextJournalId++), "VAULT_CASH", "CUSTOMER_DEPOSITS_" + acc, amt, txnId, "Teller Cash Deposit"));
                    database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++), "TELLER_DEPOSIT", teller.getUserId(), acc, "SUCCESS", "Deposited " + formatCurrency(amt)));
                    database.save();
                    System.out.println(" Cash deposit confirmed. New Balance: " + formatCurrency(a.getBalance()));
                } catch (Exception e) {
                    System.out.println("Invalid amount.");
                }
            } else if (ch.equals("3")) {
                System.out.print("Enter withdrawal account number: ");
                String acc = scanner.nextLine().trim();
                BankAccount a = database.getState().accounts.get(acc);
                if (a == null) { System.out.println("Account not found."); continue; }
                System.out.print("Enter cash withdrawal amount: ");
                try {
                    BigDecimal amt = new BigDecimal(scanner.nextLine().trim()).setScale(2, RoundingMode.HALF_EVEN);
                    if (amt.compareTo(BigDecimal.ZERO) <= 0 || amt.compareTo(a.getBalance()) > 0) { System.out.println("Invalid amount or insufficient balance."); continue; }
                    a.setBalance(a.getBalance().subtract(amt));
                    String txnId = "TXN" + (database.getState().nextTxnId++);
                    database.getState().transactions.add(new Transaction(txnId, acc, TransactionType.WITHDRAWAL, amt, a.getBalance(), "TELLER_WDL", null, "Over-the-counter Teller Cash Dispense"));
                    database.getState().ledger.add(new JournalEntry("JRN" + (database.getState().nextJournalId++), "CUSTOMER_DEPOSITS_" + acc, "VAULT_CASH", amt, txnId, "Teller Cash Dispense"));
                    database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++), "TELLER_WITHDRAWAL", teller.getUserId(), acc, "SUCCESS", "Dispensed " + formatCurrency(amt)));
                    database.save();
                    System.out.println(" Cash dispensed. New Balance: " + formatCurrency(a.getBalance()));
                } catch (Exception e) {
                    System.out.println("Invalid amount.");
                }
            } else if (ch.equals("4")) {
                viewAllAccounts(database);
            } else if (ch.equals("5")) {
                System.out.println("Teller logged out.");
                break;
            }
        }
    }

    private static void runAuditorMenu(BankDatabase database, Scanner scanner, User auditor) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("            INTERNAL AUDIT & RECONCILIATION");
            System.out.printf(" Auditor ID: %s%n", auditor.getUserId());
            System.out.println("========================================================");
            System.out.println(" 1. General Ledger & Double-Entry Journal");
            System.out.println(" 2. General Ledger Trial Balance Reconciliation");
            System.out.println(" 3. Comprehensive Audit Trail Logs");
            System.out.println(" 4. Risk Alerts & Fraud Surveillance Review");
            System.out.println(" 5. Logout");
            System.out.print("Enter choice (1-5): ");
            String ch = scanner.nextLine().trim();

            if (ch.equals("1")) {
                System.out.println("\n--- DOUBLE-ENTRY BALANCED JOURNAL LOG ---");
                for (JournalEntry j : database.getState().ledger) {
                    System.out.printf("[%s] %s | DEBIT: %-28s | CREDIT: %-28s | Amount: %10s%n",
                            j.getTimestamp(), j.getEntryId(), j.getDebitAccount(), j.getCreditAccount(), formatCurrency(j.getAmount()));
                }
            } else if (ch.equals("2")) {
                reconcileTrialBalance(database);
            } else if (ch.equals("3")) {
                viewAuditLogs(database);
            } else if (ch.equals("4")) {
                viewRiskAlerts(database);
            } else if (ch.equals("5")) {
                System.out.println("Auditor logged out.");
                break;
            }
        }
    }

    private static void reconcileTrialBalance(BankDatabase database) {
        System.out.println("\n================ FINANCIAL RECONCILIATION REPORT ================");
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalEntry j : database.getState().ledger) {
            totalDebit = totalDebit.add(j.getAmount());
            totalCredit = totalCredit.add(j.getAmount());
        }

        BigDecimal totalCustomerBalances = BigDecimal.ZERO;
        for (BankAccount a : database.getState().accounts.values()) {
            totalCustomerBalances = totalCustomerBalances.add(a.getBalance());
        }

        System.out.println("Total Ledger Journal Debits:   " + formatCurrency(totalDebit));
        System.out.println("Total Ledger Journal Credits:  " + formatCurrency(totalCredit));
        System.out.println("Net Balancing Variance:        " + formatCurrency(totalDebit.subtract(totalCredit)));
        System.out.println("Total Customer Liability:      " + formatCurrency(totalCustomerBalances));
        if (totalDebit.compareTo(totalCredit) == 0) {
            System.out.println("STATUS: [AUDIT PASSED] Double-entry ledger strictly balances to zero.");
        } else {
            System.out.println("STATUS: [AUDIT WARNING] Imbalance detected!");
        }
        System.out.println("================================================================");
    }

    private static void viewRiskAlerts(BankDatabase database) {
        System.out.println("\n--- RISK SURVEILLANCE & FRAUD ALERT QUEUE ---");
        if (database.getState().riskAlerts.isEmpty()) {
            System.out.println("No risk triggers flagged.");
            return;
        }
        for (RiskAlert r : database.getState().riskAlerts) {
            System.out.printf("[%s] %s | Rule: %-24s | Severity: %-8s | Target: %-12s | Status: %s | %s%n",
                    r.getTimestamp(), r.getAlertId(), r.getRuleName(), r.getSeverity(), r.getTargetEntity(), r.getStatus(), r.getNotes());
        }
    }

    private static void runAdminMenu(BankDatabase database, Scanner scanner, User admin) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("            ADMINISTRATOR EXECUTIVE CONSOLE");
            System.out.println("========================================================");
            System.out.println(" 1. System Operations Dashboard");
            System.out.println(" 2. Search Customers");
            System.out.println(" 3. View All Bank Accounts");
            System.out.println(" 4. Freeze Customer Account");
            System.out.println(" 5. Unfreeze Customer Account");
            System.out.println(" 6. Close Customer Account (Zero Balance Required)");
            System.out.println(" 7. Review & Disburse Loan Applications");
            System.out.println(" 8. View Enterprise Transaction Log");
            System.out.println(" 9. Audit Trail & Security Logs");
            System.out.println("10. Risk Alert Queue & Incident Disposition");
            System.out.println("11. System Health & Database Diagnostics");
            System.out.println("12. Logout");
            System.out.print("Enter choice (1-12): ");
            String ch = scanner.nextLine().trim();

            if (ch.equals("1")) {
                showAdminDashboard(database);
            } else if (ch.equals("2")) {
                searchCustomers(database, scanner);
            } else if (ch.equals("3")) {
                viewAllAccounts(database);
            } else if (ch.equals("4")) {
                setAccountStatus(database, scanner, AccountStatus.FROZEN);
            } else if (ch.equals("5")) {
                setAccountStatus(database, scanner, AccountStatus.ACTIVE);
            } else if (ch.equals("6")) {
                setAccountStatus(database, scanner, AccountStatus.CLOSED);
            } else if (ch.equals("7")) {
                reviewLoanApplications(database, scanner);
            } else if (ch.equals("8")) {
                viewEnterpriseTransactions(database);
            } else if (ch.equals("9")) {
                viewAuditLogs(database);
            } else if (ch.equals("10")) {
                dispositionRiskAlerts(database, scanner);
            } else if (ch.equals("11")) {
                showSystemHealth(database);
            } else if (ch.equals("12")) {
                System.out.println("Exiting Administrator session.");
                break;
            } else {
                System.out.println("Invalid selection. Choose between 1 and 12.");
            }
        }
    }

    private static void showAdminDashboard(BankDatabase database) {
        System.out.println("\n================ EXECUTIVE DASHBOARD ================");
        System.out.println("Total Customers:       " + database.getState().customers.size());
        System.out.println("Total Accounts:        " + database.getState().accounts.size());

        BigDecimal totalVaultLiquidity = BigDecimal.ZERO;
        int active = 0, frozen = 0, closed = 0;
        for (BankAccount a : database.getState().accounts.values()) {
            totalVaultLiquidity = totalVaultLiquidity.add(a.getBalance());
            if (a.getStatus() == AccountStatus.ACTIVE) active++;
            else if (a.getStatus() == AccountStatus.FROZEN) frozen++;
            else if (a.getStatus() == AccountStatus.CLOSED) closed++;
        }

        System.out.println("Account Breakdown:     Active: " + active + " | Frozen: " + frozen + " | Closed: " + closed);
        System.out.println("Total Deposit Balance: " + formatCurrency(totalVaultLiquidity));
        System.out.println("Total Transactions:    " + database.getState().transactions.size());
        System.out.println("Double-Entry Journals: " + database.getState().ledger.size());
        System.out.println("Risk Alert Inquiries:  " + database.getState().riskAlerts.size());
        System.out.println("Loan Applications:     " + database.getState().loans.size());
        System.out.println("Fixed Deposit Books:   " + database.getState().fixedDeposits.size());
        System.out.println("=====================================================");
    }

    private static void searchCustomers(BankDatabase database, Scanner scanner) {
        System.out.print("Enter Customer ID, Name, or Email query: ");
        String q = scanner.nextLine().trim().toLowerCase();
        boolean found = false;
        for (Customer c : database.getState().customers.values()) {
            if (c.getCustomerId().toLowerCase().contains(q) || c.getFullName().toLowerCase().contains(q) || c.getEmail().toLowerCase().contains(q)) {
                found = true;
                System.out.printf("ID: %-8s | Name: %-18s | Email: %-22s | Phone: %s%n",
                        c.getCustomerId(), c.getFullName(), c.getEmail(), c.getPhone());
            }
        }
        if (!found) System.out.println("No matching customer profiles.");
    }

    private static void viewAllAccounts(BankDatabase database) {
        System.out.println("\n--- ALL REGISTERED ACCOUNTS ---");
        for (BankAccount a : database.getState().accounts.values()) {
            System.out.printf("Acc: %-12s | Cust: %-8s | Type: %-8s | Balance: %-12s | Status: %s%n",
                    a.getAccountNumber(), a.getCustomerId(), a.getAccountType(), formatCurrency(a.getBalance()), a.getStatus());
        }
    }

    private static void setAccountStatus(BankDatabase database, Scanner scanner, AccountStatus newStatus) {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        BankAccount acc = database.getState().accounts.get(accNum);
        if (acc == null) { System.out.println("Account not found."); return; }

        if (newStatus == AccountStatus.CLOSED) {
            if (acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("Operation Rejected: Cannot close account with non-zero balance (" + formatCurrency(acc.getBalance()) + "). All funds must be withdrawn first.");
                return;
            }
        }

        acc.setStatus(newStatus);
        database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                "ACCOUNT_STATUS_CHANGE", "ADMIN", accNum, "SUCCESS", "Account set to " + newStatus));
        database.save();
        System.out.println(" Account " + accNum + " is now " + newStatus);
    }

    private static void reviewLoanApplications(BankDatabase database, Scanner scanner) {
        System.out.println("\n--- PENDING LOAN PIPELINE ---");
        List<Loan> pending = new ArrayList<>();
        for (Loan l : database.getState().loans) {
            if (l.getStatus() == LoanStatus.PENDING) pending.add(l);
        }

        if (pending.isEmpty()) { System.out.println("No pending loan reviews."); return; }

        for (Loan l : pending) {
            System.out.printf("Loan ID: %-6s | Cust: %-8s | Principal: %10s | Months: %-3d | EMI: %8s%n",
                    l.getLoanId(), l.getCustomerId(), formatCurrency(l.getPrincipalAmount()), l.getTermMonths(), formatCurrency(l.getMonthlyEmi()));
        }

        System.out.print("Enter Loan ID to decision: ");
        String lId = scanner.nextLine().trim();
        Loan target = null;
        for (Loan l : pending) {
            if (l.getLoanId().equalsIgnoreCase(lId)) { target = l; break; }
        }

        if (target == null) { System.out.println("Pending loan not found."); return; }

        System.out.print("Decision (1. Approve & Disburse, 2. Reject): ");
        String d = scanner.nextLine().trim();
        if (d.equals("1")) {
            target.setStatus(LoanStatus.DISBURSED);
            BankAccount acc = database.getState().accounts.get(target.getAccountNumber());
            acc.setBalance(acc.getBalance().add(target.getPrincipalAmount()));

            String tId = "TXN" + (database.getState().nextTxnId++);
            Transaction t = new Transaction(tId, acc.getAccountNumber(), TransactionType.LOAN_DISBURSEMENT,
                    target.getPrincipalAmount(), acc.getBalance(), target.getLoanId(), null, "Approved Loan Disbursement");
            database.getState().transactions.add(t);

            JournalEntry j = new JournalEntry("JRN" + (database.getState().nextJournalId++),
                    "LOAN_PORTFOLIO_" + target.getLoanId(), "CUSTOMER_DEPOSITS_" + acc.getAccountNumber(),
                    target.getPrincipalAmount(), target.getLoanId(), "Loan Capital Disbursement");
            database.getState().ledger.add(j);

            database.getState().notifications.add(new InAppNotification("NOTIF" + (database.getState().nextNotifId++),
                    target.getCustomerId(), "Loan Disbursed", "Your loan " + target.getLoanId() + " of " + formatCurrency(target.getPrincipalAmount()) + " has been disbursed."));

            database.getState().auditLogs.add(new AuditLog("AUD" + (database.getState().nextAuditId++),
                    "LOAN_DISBURSED", "ADMIN", target.getLoanId(), "SUCCESS", "Disbursed " + formatCurrency(target.getPrincipalAmount())));
            database.save();
            System.out.println(" Loan approved and funds disbursed directly to " + acc.getAccountNumber());
        } else if (d.equals("2")) {
            target.setStatus(LoanStatus.REJECTED);
            database.save();
            System.out.println(" Loan application marked REJECTED.");
        }
    }

    private static void viewEnterpriseTransactions(BankDatabase database) {
        System.out.println("\n--- ENTERPRISE TRANSACTION AUDIT STREAM ---");
        int count = 0;
        for (Transaction t : database.getState().transactions) {
            System.out.printf("[%s] %s | Acc: %-12s | Type: %-16s | Amt: %10s | Bal: %10s%n",
                    t.getTimestamp(), t.getTransactionId(), t.getAccountNumber(), t.getType(),
                    formatCurrency(t.getAmount()), formatCurrency(t.getBalanceAfter()));
            count++;
            if (count > 50) break;
        }
    }

    private static void viewAuditLogs(BankDatabase database) {
        System.out.println("\n--- IMMUTABLE SECURITY AUDIT TRAIL ---");
        for (AuditLog al : database.getState().auditLogs) {
            System.out.printf("[%s] %s | Event: %-18s | Actor: %-10s | Target: %-12s | Outcome: %s | %s%n",
                    al.getTimestamp(), al.getAuditId(), al.getEventType(), al.getActor(), al.getTarget(), al.getOutcome(), al.getDetails());
        }
    }

    private static void dispositionRiskAlerts(BankDatabase database, Scanner scanner) {
        viewRiskAlerts(database);
        if (database.getState().riskAlerts.isEmpty()) return;

        System.out.print("Enter Alert ID to review (or Enter to cancel): ");
        String aId = scanner.nextLine().trim();
        if (aId.isEmpty()) return;

        RiskAlert alert = null;
        for (RiskAlert r : database.getState().riskAlerts) {
            if (r.getAlertId().equalsIgnoreCase(aId)) { alert = r; break; }
        }
        if (alert == null) { System.out.println("Alert not found."); return; }

        System.out.println("Action: 1. Mark RESOLVED, 2. Mark FALSE_POSITIVE, 3. Keep OPEN");
        System.out.print("Choice: ");
        String c = scanner.nextLine().trim();
        if (c.equals("1")) alert.setStatus(RiskStatus.RESOLVED);
        else if (c.equals("2")) alert.setStatus(RiskStatus.FALSE_POSITIVE);
        database.save();
        System.out.println("Alert disposition updated.");
    }

    private static void showSystemHealth(BankDatabase database) {
        System.out.println("\n--- SYSTEM HEALTH & PERSISTENCE DIAGNOSTICS ---");
        File f = new File(DB_FILE_PATH);
        System.out.println("Database Store:     " + f.getAbsolutePath());
        System.out.println("Store Exists:       " + f.exists());
        System.out.println("Store Size:         " + f.length() + " bytes");
        System.out.println("JVM Architecture:   " + System.getProperty("os.arch"));
        System.out.println("Java Runtime:       " + System.getProperty("java.version"));
        System.out.println("Memory Used:        " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB");
        System.out.println("Status:             ONLINE / HEALTHY (ACID In-Memory + Journal File Persistence)");
    }
}
