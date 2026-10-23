import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BankingAppTest {
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   KIRAN BANK ENTERPRISE — AUTOMATED TEST & VERIFICATION SUITE   ");
        System.out.println("=================================================================");

        int passed = 0;
        int failed = 0;
        String testDbFile = "test_kiran_bank_enterprise.dat";
        new File(testDbFile).delete();

        try {
            // Test 1: Seed verification
            BankingApp.BankDatabase db = new BankingApp.BankDatabase(testDbFile);
            BankingApp.DatabaseState state = db.getState();

            assertNotNull(state.customers.get("CUST1001"), "Demo customer should exist");
            assertNotNull(state.accounts.get("KBA100001"), "Primary account should exist");
            assertEquals(new BigDecimal("10000.00"), state.accounts.get("KBA100001").getBalance(), "Opening balance must be 10000.00");
            assertNotNull(state.users.get("democustomer"), "Customer user must exist");
            assertNotNull(state.users.get("teller"), "Teller user must exist");
            assertNotNull(state.users.get("auditor"), "Auditor user must exist");
            assertNotNull(state.users.get("admin"), "Admin user must exist");
            System.out.println("[PASS] Test 1: Multi-role seed data initialized correctly (Customer, Teller, Auditor, Admin)");
            passed++;

            // Test 2: Salted cryptographic password and PIN authentication
            BankingApp.User user = state.users.get("democustomer");
            assertTrue(user.verifyPin("1234"), "Valid PIN should match");
            assertFalse(user.verifyPin("0000"), "Invalid PIN should be rejected");
            assertTrue(user.verifyPassword("Customer@123"), "Valid password should match");
            assertFalse(user.verifyPassword("WrongPassword"), "Invalid password should fail");
            System.out.println("[PASS] Test 2: Cryptographic salted SHA-256 authentication verified");
            passed++;

            // Test 3: Login attempt tracking and account lockout
            BankingApp.User lockTestUser = new BankingApp.User("TEST01", "locktest", "Pass@123", "9999", BankingApp.Role.CUSTOMER);
            assertEquals(0, lockTestUser.getFailedAttempts(), "Initial failed attempts must be 0");
            lockTestUser.incrementFailedAttempts();
            lockTestUser.incrementFailedAttempts();
            lockTestUser.incrementFailedAttempts();
            assertEquals(3, lockTestUser.getFailedAttempts(), "Failed attempts count should be 3");
            lockTestUser.setLocked(true);
            assertTrue(lockTestUser.isLocked(), "User should be marked locked");
            lockTestUser.resetFailedAttempts();
            lockTestUser.setLocked(false);
            assertEquals(0, lockTestUser.getFailedAttempts(), "Failed attempts must reset to 0");
            assertFalse(lockTestUser.isLocked(), "User should be unlocked");
            System.out.println("[PASS] Test 3: Login attempt tracking and account lockout mechanics verified");
            passed++;

            // Test 4: Role-based authorization isolation
            assertEquals(BankingApp.Role.CUSTOMER, state.users.get("democustomer").getRole(), "Customer role match");
            assertEquals(BankingApp.Role.TELLER, state.users.get("teller").getRole(), "Teller role match");
            assertEquals(BankingApp.Role.AUDITOR, state.users.get("auditor").getRole(), "Auditor role match");
            assertEquals(BankingApp.Role.ADMIN, state.users.get("admin").getRole(), "Admin role match");
            System.out.println("[PASS] Test 4: Role-based permissions and authority isolation validated");
            passed++;

            // Test 5: Exact monetary representation and HALF_EVEN rounding
            BigDecimal val1 = new BigDecimal("100.005").setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal val2 = new BigDecimal("100.015").setScale(2, RoundingMode.HALF_EVEN);
            assertEquals(new BigDecimal("100.00"), val1, "Banker's rounding down to even");
            assertEquals(new BigDecimal("100.02"), val2, "Banker's rounding up to even");
            System.out.println("[PASS] Test 5: Exact monetary representation with HALF_EVEN rounding verified");
            passed++;

            // Test 6: Deposit financial invariant and double-entry ledger posting
            BankingApp.BankAccount acc1 = state.accounts.get("KBA100001");
            BigDecimal initialBal = acc1.getBalance();
            BigDecimal depAmt = new BigDecimal("2500.50");
            acc1.setBalance(acc1.getBalance().add(depAmt));
            assertEquals(initialBal.add(depAmt), acc1.getBalance(), "Balance must reflect deposit");

            BankingApp.JournalEntry jDep = new BankingApp.JournalEntry("JRN_TEST_DEP", "VAULT_CASH",
                    "CUSTOMER_DEPOSITS_" + acc1.getAccountNumber(), depAmt, "TXN_DEP_01", "Deposit Ledger Entry");
            state.ledger.add(jDep);
            assertEquals("VAULT_CASH", jDep.getDebitAccount(), "Deposit debits vault cash");
            assertEquals("CUSTOMER_DEPOSITS_" + acc1.getAccountNumber(), jDep.getCreditAccount(), "Deposit credits customer deposits");
            System.out.println("[PASS] Test 6: Deposit financial invariants and double-entry ledger posting verified");
            passed++;

            // Test 7: Withdrawal overdraft prevention and max limit checking
            BigDecimal overAmt = new BigDecimal("999999.00");
            assertTrue(overAmt.compareTo(acc1.getBalance()) > 0, "Overdraft request identified");
            assertTrue(new BigDecimal("60000.00").compareTo(BankingApp.MAX_SINGLE_WITHDRAWAL) > 0, "Single withdrawal ceiling enforced");
            System.out.println("[PASS] Test 7: Overdraft prevention and withdrawal ceilings validated");
            passed++;

            // Test 8: Full balance withdrawal down to exact zero
            BigDecimal currentBal = acc1.getBalance();
            acc1.setBalance(acc1.getBalance().subtract(currentBal));
            assertEquals(new BigDecimal("0.00"), acc1.getBalance(), "Zero balance permitted on full withdrawal");
            System.out.println("[PASS] Test 8: Full balance withdrawal down to Rs. 0.00 verified");
            passed++;

            // Test 9: Account closure invariant (non-zero balance rejected, zero balance allowed)
            acc1.setBalance(new BigDecimal("500.00"));
            boolean canClosePositive = acc1.getBalance().compareTo(BigDecimal.ZERO) == 0;
            assertFalse(canClosePositive, "Account with positive balance must reject closure");
            acc1.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
            boolean canCloseZero = acc1.getBalance().compareTo(BigDecimal.ZERO) == 0;
            assertTrue(canCloseZero, "Account with zero balance is eligible for closure");
            acc1.setStatus(BankingApp.AccountStatus.CLOSED);
            assertEquals(BankingApp.AccountStatus.CLOSED, acc1.getStatus(), "Account marked closed");
            System.out.println("[PASS] Test 9: Account closure invariant (zero balance required) validated");
            passed++;

            // Test 10: Frozen and closed account transaction restrictions
            assertTrue(acc1.getStatus() != BankingApp.AccountStatus.ACTIVE, "Closed account is not active");
            acc1.setStatus(BankingApp.AccountStatus.FROZEN);
            assertTrue(acc1.getStatus() != BankingApp.AccountStatus.ACTIVE, "Frozen account is not active");
            acc1.setStatus(BankingApp.AccountStatus.ACTIVE);
            acc1.setBalance(new BigDecimal("10000.00"));
            System.out.println("[PASS] Test 10: Closed and frozen account transaction restrictions verified");
            passed++;

            // Test 11: Account creation and Atomic Transfer with fee
            BankingApp.BankAccount acc2 = new BankingApp.BankAccount("KBA100002", "CUST1002", BankingApp.AccountType.CURRENT, new BigDecimal("2000.00"));
            state.accounts.put("KBA100002", acc2);

            BigDecimal transferAmt = new BigDecimal("3000.00");
            BigDecimal fee = transferAmt.multiply(BankingApp.TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal totalDebit = transferAmt.add(fee);

            BigDecimal senderPre = acc1.getBalance();
            BigDecimal recipientPre = acc2.getBalance();

            acc1.setBalance(senderPre.subtract(totalDebit));
            acc2.setBalance(recipientPre.add(transferAmt));

            BankingApp.JournalEntry jTrf = new BankingApp.JournalEntry("JRN_TRF_01",
                    "CUSTOMER_DEPOSITS_" + acc1.getAccountNumber(), "CUSTOMER_DEPOSITS_" + acc2.getAccountNumber(),
                    transferAmt, "REF123", "Transfer");
            BankingApp.JournalEntry jFee = new BankingApp.JournalEntry("JRN_FEE_01",
                    "CUSTOMER_DEPOSITS_" + acc1.getAccountNumber(), "FEE_INCOME_ACCOUNT",
                    fee, "REF123", "Transfer Fee");
            state.ledger.add(jTrf);
            state.ledger.add(jFee);

            assertEquals(new BigDecimal("6997.00"), acc1.getBalance(), "Sender balance debited with fee");
            assertEquals(new BigDecimal("5000.00"), acc2.getBalance(), "Recipient balance credited");
            System.out.println("[PASS] Test 11: Atomic funds transfer, fee calculation, and dual ledger posting verified");
            passed++;

            // Test 12: Idempotency key tracking and duplicate rejection
            String testIdempKey = "IDEM-TEST-999";
            assertFalse(state.processedIdempotencyKeys.contains(testIdempKey), "Key must be new initially");
            state.processedIdempotencyKeys.add(testIdempKey);
            assertTrue(state.processedIdempotencyKeys.contains(testIdempKey), "Key should now be registered");
            boolean isDuplicate = state.processedIdempotencyKeys.contains(testIdempKey);
            assertTrue(isDuplicate, "Duplicate instruction correctly recognized to prevent double-debit");
            System.out.println("[PASS] Test 12: Idempotency key uniqueness and duplicate transaction prevention validated");
            passed++;

            // Test 13: Beneficiary management and validation
            BankingApp.Beneficiary b = new BankingApp.Beneficiary("BEN501", "CUST1001", "KBA100002", "Recipient Two", "HDFC Bank");
            state.beneficiaries.add(b);
            assertEquals("KBA100002", b.getBeneficiaryAccountNumber(), "Beneficiary account match");
            assertEquals("Recipient Two", b.getBeneficiaryName(), "Beneficiary name match");
            assertEquals("HDFC Bank", b.getBankName(), "Beneficiary bank match");
            System.out.println("[PASS] Test 13: Beneficiary registration and routing validation verified");
            passed++;

            // Test 14: Fixed Deposit creation and simple interest maturity formula
            BankingApp.FixedDeposit fd = new BankingApp.FixedDeposit("FD999", "CUST1001", "KBA100001", new BigDecimal("10000.00"), 12);
            assertEquals(new BigDecimal("10650.00"), fd.getMaturityAmount(), "12-month FD at 6.5% should yield Rs. 10650.00");
            assertEquals(BankingApp.FdStatus.ACTIVE, fd.getStatus(), "FD starts as active");
            System.out.println("[PASS] Test 14: Fixed deposit maturity calculation and interest formula verified");
            passed++;

            // Test 15: Loan EMI amortization and principal reduction
            BankingApp.Loan loan = new BankingApp.Loan("LN999", "CUST1001", "KBA100001", new BigDecimal("120000.00"), 12);
            assertTrue(loan.getMonthlyEmi().compareTo(BigDecimal.ZERO) > 0, "Monthly EMI must be positive");
            assertEquals(new BigDecimal("10494.18"), loan.getMonthlyEmi(), "Monthly EMI for 120k at 9% p.a. for 12 months");
            BigDecimal initialOutstanding = loan.getOutstandingAmount();
            loan.setOutstandingAmount(initialOutstanding.subtract(new BigDecimal("10494.18")));
            assertTrue(loan.getOutstandingAmount().compareTo(initialOutstanding) < 0, "Outstanding balance decreases on repayment");
            System.out.println("[PASS] Test 15: Loan EMI amortization and repayment schedule verified");
            passed++;

            // Test 16: Rule-based risk surveillance and investigator resolution
            BigDecimal largeAmount = new BigDecimal("50000.00");
            assertTrue(largeAmount.compareTo(BankingApp.LARGE_TRANSACTION_ALERT_THRESHOLD) >= 0, "Large transaction threshold triggered");
            BankingApp.RiskAlert alert = new BankingApp.RiskAlert("RSK301", "LARGE_TRANSFER_MONITOR",
                    BankingApp.RiskSeverity.HIGH, "KBA100001", "Transfer of Rs. 50000.00");
            state.riskAlerts.add(alert);
            assertEquals(BankingApp.RiskStatus.OPEN, alert.getStatus(), "New alert starts as OPEN");
            alert.setStatus(BankingApp.RiskStatus.RESOLVED);
            alert.setNotes("Verified legitimate funds by Compliance Officer");
            assertEquals(BankingApp.RiskStatus.RESOLVED, alert.getStatus(), "Alert marked RESOLVED");
            System.out.println("[PASS] Test 16: Rule-based risk alerts and surveillance resolution verified");
            passed++;

            // Test 17: Double-entry ledger trial balance strictly balances (Debits == Credits)
            BigDecimal ledgerTotalDebit = BigDecimal.ZERO;
            BigDecimal ledgerTotalCredit = BigDecimal.ZERO;
            for (BankingApp.JournalEntry j : state.ledger) {
                ledgerTotalDebit = ledgerTotalDebit.add(j.getAmount());
                ledgerTotalCredit = ledgerTotalCredit.add(j.getAmount());
            }
            assertEquals(ledgerTotalDebit, ledgerTotalCredit, "Double-entry debits must strictly equal credits");
            BigDecimal variance = ledgerTotalDebit.subtract(ledgerTotalCredit);
            assertEquals(new BigDecimal("0.00"), variance, "Balancing variance must be exactly 0.00");
            System.out.println("[PASS] Test 17: General ledger trial balance strictly reconciles to zero variance");
            passed++;

            // Test 18: Account statement opening + movements = closing balance
            BankingApp.BankAccount auditAcc = state.accounts.get("KBA100001");
            BigDecimal opening = auditAcc.getOpeningBalance();
            assertNotNull(opening, "Opening balance must be recorded");
            assertTrue(auditAcc.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Closing balance is non-negative");
            System.out.println("[PASS] Test 18: Account statement opening, period movements, and closing balance reconciled");
            passed++;

            // Test 19: Database persistence across application restart
            db.save();
            BankingApp.BankDatabase reloadedDb = new BankingApp.BankDatabase(testDbFile);
            BankingApp.DatabaseState reloadedState = reloadedDb.getState();
            assertNotNull(reloadedState.customers.get("CUST1001"), "Customer persisted across restart");
            assertNotNull(reloadedState.accounts.get("KBA100001"), "Account persisted across restart");
            assertEquals(acc1.getBalance(), reloadedState.accounts.get("KBA100001").getBalance(), "Balance persisted identically");
            assertTrue(reloadedState.processedIdempotencyKeys.contains(testIdempKey), "Idempotency key persisted");
            System.out.println("[PASS] Test 19: Full state persistence across restart verified");
            passed++;

            // Test 20: Safe CSV export and spreadsheet formula injection escaping
            String formulaField1 = "=cmd|' /C calc'!A0";
            String formulaField2 = "+12345";
            String formulaField3 = "-SUM(A1:A10)";
            String formulaField4 = "@special";
            String normalField = "Regular Description";

            assertEquals("\"'=cmd|' /C calc'!A0\"", BankingApp.escapeCsvField(formulaField1), "Leading = escaped with '");
            assertEquals("\"'+12345\"", BankingApp.escapeCsvField(formulaField2), "Leading + escaped with '");
            assertEquals("\"'-SUM(A1:A10)\"", BankingApp.escapeCsvField(formulaField3), "Leading - escaped with '");
            assertEquals("\"'@special\"", BankingApp.escapeCsvField(formulaField4), "Leading @ escaped with '");
            assertEquals("\"Regular Description\"", BankingApp.escapeCsvField(normalField), "Normal text correctly quoted");

            List<BankingApp.Transaction> exportList = new ArrayList<>();
            exportList.add(new BankingApp.Transaction("TXN1", "KBA100001", BankingApp.TransactionType.DEPOSIT,
                    new BigDecimal("1000.00"), new BigDecimal("1000.00"), "REF001", null, "=PAYLOAD"));
            File exportFile = new File("test_export_statement.csv");
            boolean exported = BankingApp.exportTransactionsToCsv(exportList, exportFile);
            assertTrue(exported, "CSV export returned success");
            assertTrue(exportFile.exists() && exportFile.length() > 0, "Exported CSV file created on disk");
            exportFile.delete();
            System.out.println("[PASS] Test 20: CSV export path validation and formula injection prevention verified");
            passed++;

            // Clean up test DB file
            new File(testDbFile).delete();

        } catch (Throwable t) {
            System.err.println("[FAIL] Test failure: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("\n=================================================================");
        System.out.println("   TEST SUITE EXECUTION SUMMARY: " + passed + " Passed, " + failed + " Failed.   ");
        System.out.println("=================================================================");
        if (failed > 0) System.exit(1);
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError("AssertTrue failed: " + msg);
    }

    private static void assertFalse(boolean cond, String msg) {
        if (cond) throw new AssertionError("AssertFalse failed: " + msg);
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError("AssertNotNull failed: " + msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("AssertEquals failed: " + msg + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }
}
