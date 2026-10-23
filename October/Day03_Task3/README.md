# Kiran Bank Enterprise v4.0 — Master Banking Management Platform

An educational, enterprise-grade Console Banking Management System built in **Java 17+ LTS**. This platform demonstrates production-style banking engineering principles: layered architecture, strict monetary precision via `BigDecimal`, balanced double-entry general ledger, cryptographic role-based access control (RBAC), transaction idempotency, fraud surveillance heuristics, loan amortization, fixed deposit accrual, and comprehensive automated test suites.

---

## 1. Executive Summary & Core Metrics

| Metric / Parameter | Value / Specification |
|:---|:---|
| **Platform Name** | Kiran Bank Enterprise |
| **System Version** | `4.0.0-LTS` |
| **Target Runtime** | Java 17 LTS / Java 21 LTS |
| **Monetary Arithmetic** | `BigDecimal` (Scale 2, `HALF_EVEN` Banker's Rounding) |
| **Core Accounting** | Balanced Double-Entry General Ledger ($\sum \text{Debits} = \sum \text{Credits}$) |
| **Authentication Model** | Salted SHA-256 (`SecureRandom` 16-byte salt) + PIN + 3-attempt Lockout |
| **Supported Roles** | `CUSTOMER`, `TELLER`, `AUDITOR`, `ADMIN` |
| **Primary Persistence** | Atomic Binary State Journal (`kiran_bank_enterprise_db.dat`) |
| **Automated Test Coverage** | 20 / 20 Automated Enterprise Invariant Tests Passing (100%) |

---

## 2. System Architecture

The application strictly adheres to a multi-tiered layered design separating Presentation, Application Services, Domain Models, Persistence, and Auditing:

```mermaid
graph TD
    subgraph Presentation Layer
        UI_C[Customer Portal]
        UI_T[Teller Operations Desk]
        UI_A[Internal Audit Console]
        UI_M[Admin Executive Console]
    end

    subgraph Service & Security Layer
        AUTH[Salted SHA-256 RBAC Engine]
        TXN[Atomic Transaction & Idempotency Manager]
        RISK[Rule-Based Risk Surveillance Engine]
        LEDGER[Double-Entry General Ledger Reconciler]
    end

    subgraph Domain & Entity Models
        ACC[BankAccount: Savings / Current]
        LN[Loan Portfolio & EMI Amortization]
        FD[Fixed Deposit Certificates]
        AUD[Immutable Security Audit Trail]
    end

    subgraph Persistence Layer
        DB[(State Journal: kiran_bank_enterprise_db.dat)]
        MIG[Schema Definitions & SQLite JDBC Mapping]
    end

    Presentation Layer --> Service & Security Layer
    Service & Security Layer --> Domain & Entity Models
    Domain & Entity Models --> Persistence Layer
```

---

## 3. Role-Based Access Control (RBAC) & Portal Directory

The system provides 4 strictly isolated portals. Administrative and operational privileges cannot be forged via request tags and are enforced strictly via authenticated session objects:

| Role | Portal Identifier | Allowed Operations | Restricted / Prohibited Actions |
|:---|:---|:---|:---|
| **`CUSTOMER`** | Self-Service Portal | Balance check, cash deposit, cash withdrawal, account-to-account funds transfer, beneficiary directory, account statements, loan application, fixed deposit creation, PIN/Password reset. | Cannot view other customers' accounts, access vault ledger, alter loan states, or disposition fraud alerts. |
| **`TELLER`** | Counter Operations Desk | Process cash deposits, withdrawals, customer registration, account balance lookups for counter visitors. | Cannot execute maker-checker loan approvals, freeze accounts, or view trial balance audit journals. |
| **`AUDITOR`** | Audit & Reconciliation Desk | Read-only inspection of double-entry ledger, trial balance reconciliation verification, complete audit trail logs, fraud risk queue review. | Read-only access: Cannot initiate financial movements, modify account balances, or approve loans. |
| **`ADMIN`** | Executive Console | System operations dashboard, customer lookup, account freeze/unfreeze/closure, loan underwriting & disbursement, fraud alert disposition, database diagnostics. | Cannot bypass dual-entry ledger balancing or close accounts with non-zero balances. |

---

## 4. Demo Credentials & Test Accounts

The platform seeds pre-configured test users and accounts upon initial startup:

| Role | Username | Password | PIN | Default Account | Opening Balance | Scope |
|:---|:---|:---|:---|:---|:---|:---|
| **Customer** | `democustomer` | `Customer@123` | `1234` | `KBA100001` (Savings) | Rs. 10,000.00 | Customer self-service operations |
| **Teller** | `teller` | `Teller@123` | `5555` | — | Cash Counter | Counter operations desk |
| **Auditor** | `auditor` | `Auditor@123` | `7777` | — | Read-Only Audit | General Ledger & Trial Balance |
| **Administrator** | `admin` | `Admin@123` | `9999` | — | Executive Vault | Core bank administration |

---

## 5. Core Banking Products & Transaction Policies

### 5.1 Transaction Limits & Operational Ceilings

| Transaction Type | Minimum Limit | Maximum Single Limit | Applicable Service Fee | Daily Limits Enforced |
|:---|:---|:---|:---|:---|
| **Cash Deposit** | Rs. 1.00 | Unlimited | Rs. 0.00 (Free) | No limit |
| **Cash Withdrawal** | Rs. 1.00 | Rs. 50,000.00 | Rs. 0.00 (Free) | Overdraft strictly prohibited |
| **Internal Transfer** | Rs. 1.00 | Rs. 100,000.00 | 0.10% (`0.001`) of transfer amount | Idempotency key tracking enabled |
| **Loan Disbursement** | Rs. 10,000.00 | Rs. 1,000,000.00 | Rs. 0.00 | Requires Admin Approval |
| **Fixed Deposit** | Rs. 1,000.00 | Rs. 5,000,000.00 | Rs. 0.00 | Locked until maturity |

### 5.2 Account Lifecycles & Closure Invariants

| Status | Permitted Operations | Prohibited Operations | Transition Rules |
|:---|:---|:---|:---|
| **`ACTIVE`** | All standard banking transactions: Deposits, Withdrawals, Transfers, Loans, FDs. | None. | Default state upon successful customer onboarding. |
| **`FROZEN`** | Profile inspection, balance inquiry, statement generation. | Cash withdrawals, outgoing transfers, debit card charges. | Flagged manually by Admin or automatically on suspicious activity. Can be unfrozen by Admin. |
| **`CLOSED`** | Historical statement review only. | All credits, debits, transfers, loan applications, and interest postings. | **Strict Invariant**: Requires an exact **Rs. 0.00 balance**. Accounts with remaining funds reject closure until all funds are withdrawn. |

---

## 6. Double-Entry General Ledger (Chart of Accounts)

Every financial movement creates atomic, balanced debit and credit journal lines:

| Financial Operation | Debit Line (Account) | Credit Line (Account) | Accounting Rationalization |
|:---|:---|:---|:---|
| **Cash Deposit** | `VAULT_CASH` | `CUSTOMER_DEPOSITS_<acc>` | Cash enters vault asset; increases bank liability to customer. |
| **Cash Withdrawal** | `CUSTOMER_DEPOSITS_<acc>` | `VAULT_CASH` | Reduces customer deposit liability; cash leaves vault asset. |
| **Account Transfer** | `CUSTOMER_DEPOSITS_<sender>` | `CUSTOMER_DEPOSITS_<recipient>` | Liabilities transfer from sender account to recipient account. |
| **Transfer Fee** | `CUSTOMER_DEPOSITS_<sender>` | `FEE_INCOME_ACCOUNT` | Reduces sender liability; credited to bank operating revenue. |
| **Loan Disbursement** | `LOAN_PORTFOLIO_<loanId>` | `CUSTOMER_DEPOSITS_<acc>` | Loan asset created on bank books; funds credited to customer account. |
| **Loan Repayment** | `CUSTOMER_DEPOSITS_<acc>` | `LOAN_PORTFOLIO_<loanId>` | Reduces customer liability; decreases outstanding loan asset. |

> **Auditor Verification Rule**: At any point in time, the Auditor console verifies:
> $$\sum \text{Ledger Debits} - \sum \text{Ledger Credits} = 0.00$$

---

## 7. Loan Products & Amortization Mechanics

The loan engine supports full lifecycle underwriting, amortization calculation, disbursement, and repayment:

| Parameter | Standard Retail Loan Product |
|:---|:---|
| **Annual Interest Rate ($R$)** | 9.00% p.a. (`0.09`) |
| **Supported Tenures ($n$)** | 6, 12, 24, 36, or 60 Months |
| **Monthly Interest Rate ($r$)** | $r = \frac{R}{12} = \frac{0.09}{12} = 0.0075$ |
| **Monthly EMI Formula** | $\text{EMI} = \frac{P \times r \times (1 + r)^n}{(1 + r)^n - 1}$ |
| **Disbursement Invariant** | Disburses directly into the applicant's linked savings or current account upon Admin approval. |
| **Closure Rule** | Marked `CLOSED` immediately when total repayments equal or exceed total amortized repayable value. |

---

## 8. Fixed Deposit Product Specifications

| Attribute | Specification |
|:---|:---|
| **Annual Return Rate** | 6.50% p.a. (`0.065`) |
| **Tenure Options** | 6 Months, 12 Months, 24 Months, 36 Months |
| **Interest Calculation** | $I = \frac{P \times R \times T}{12}$ |
| **Maturity Calculation** | $A = P + I$ (Exact decimal representation) |
| **Maturity Example (12 Mo, Rs. 10,000)** | Principal: Rs. 10,000.00 \| Interest: Rs. 650.00 \| **Maturity Value: Rs. 10,650.00** |
| **Double-Liquidation Invariant** | Unique certificate reference ID prevents double-liquidation. |

---

## 9. Rule-Based Fraud Surveillance & Risk Monitoring

The platform includes explainable, deterministic rule-based surveillance:

| Rule Identifier | Trigger Condition | Severity | Automated Action Taken |
|:---|:---|:---|:---|
| `LARGE_TRANSFER_MONITOR` | Single transfer amount $\ge \text{Rs. 45,000.00}$ | `HIGH` | Generates `RSK` alert; queues for compliance review. |
| `LARGE_DEPOSIT_MONITOR` | Single cash deposit $\ge \text{Rs. 45,000.00}$ | `MEDIUM` | Generates surveillance record in audit log. |
| `FAILED_LOGIN_SPIKE` | 3 consecutive invalid password/PIN attempts | `CRITICAL` | Account temporarily locked; security audit log emitted. |
| `RAPID_RETRY_IDEMPOTENCY` | Identical transfer request re-submitted | `LOW` | Rejected under Idempotency rules; logged without debit. |

---

## 10. Safe CSV Export & Formula Injection Prevention

To protect downstream spreadsheet applications (Microsoft Excel, LibreOffice Calc, Google Sheets), exported CSV fields are sanitized:

| Payload Type | Example Vulnerable Input | Sanitized Output in CSV | Protective Effect |
|:---|:---|:---|:---|
| **Formula Injection (`=`)** | `=cmd\|' /C calc'!A0` | `"'=cmd\|' /C calc'!A0"` | Disarms executable formula execution. |
| **DDE Execution (`+`)** | `+12345` | `"'+12345"` | Forces spreadsheet parser to treat as plain text. |
| **Formula Subtraction (`-`)** | `-SUM(A1:A10)` | `"'-SUM(A1:A10)"` | Prevents unauthorized cross-sheet formula calculation. |
| **Function Call (`@`)** | `@special()` | `"'@special()"` | Prevents external macro lookup. |
| **Standard Text** | `Regular Description` | `"Regular Description"` | Wrapped in RFC-4180 standard escaping. |

---

## 11. Database Schema & Data Dictionary

| Table / Entity | Primary Key | Foreign Keys | Key Attributes & Constraints |
|:---|:---|:---|:---|
| **`USERS`** | `user_id` | — | `username` (UNIQUE), `password_hash`, `password_salt`, `pin_hash`, `pin_salt`, `role`, `locked` |
| **`CUSTOMERS`** | `customer_id` | — | `full_name`, `email` (UNIQUE), `phone`, `status`, `created_at` |
| **`ACCOUNTS`** | `account_number` | `customer_id` | `account_type`, `balance` (CHECK >= 0), `opening_balance`, `status` (`ACTIVE`/`FROZEN`/`CLOSED`) |
| **`TRANSACTIONS`** | `transaction_id` | `account_number` | `type`, `amount`, `balance_after`, `timestamp`, `reference_id`, `idempotency_key` (UNIQUE) |
| **`JOURNAL_ENTRIES`** | `entry_id` | — | `timestamp`, `debit_account`, `credit_account`, `amount`, `reference`, `description` |
| **`BENEFICIARIES`** | `beneficiary_id` | `customer_id` | `beneficiary_acc_num`, `beneficiary_name`, `bank_name` |
| **`LOANS`** | `loan_id` | `customer_id`, `account_number` | `principal_amount`, `term_months`, `annual_rate`, `monthly_emi`, `outstanding_amount`, `status` |
| **`FIXED_DEPOSITS`** | `fd_id` | `customer_id`, `account_number` | `principal_amount`, `tenure_months`, `maturity_amount`, `start_date`, `maturity_date`, `status` |
| **`RISK_ALERTS`** | `alert_id` | — | `rule_name`, `severity`, `target_entity`, `status`, `notes`, `timestamp` |
| **`NOTIFICATIONS`** | `notification_id` | `customer_id` | `title`, `message`, `timestamp`, `read` |
| **`AUDIT_LOGS`** | `log_id` | — | `action_type`, `actor_id`, `target_entity`, `status`, `timestamp`, `details` |

---

## 12. Automated Test Suite Matrix (20 Enterprise Tests)

The test suite in [`BankingAppTest.java`](file:///d:/kiran-academy-java-internship/October/Day03_Task3/BankingAppTest.java) verifies all system invariants:

| # | Test Name | Verification Focus | Result |
|:---:|:---|:---|:---:|
| **1** | `testSeedDataInitialization` | Initial users (Customer, Teller, Auditor, Admin), accounts, and vault balances exist. | **PASS** |
| **2** | `testSaltedAuthentication` | Salted SHA-256 matching for correct password/PIN; mismatch rejection on incorrect inputs. | **PASS** |
| **3** | `testLoginLockoutMechanics` | 3 consecutive failed authentication attempts properly lock user credential state. | **PASS** |
| **4** | `testRolePermissionsIsolation` | Customer, Teller, Auditor, and Admin roles maintain strict privilege boundaries. | **PASS** |
| **5** | `testPrecisionMonetaryRounding` | `BigDecimal` arithmetic with `HALF_EVEN` rounding verifies exact banker's precision. | **PASS** |
| **6** | `testDepositLedgerPosting` | Cash deposits increase customer balance and post balanced `VAULT_CASH` ledger entry. | **PASS** |
| **7** | `testWithdrawalOverdraftCeiling` | Overdraft attempts and amounts exceeding the single withdrawal ceiling are rejected. | **PASS** |
| **8** | `testExactZeroWithdrawal` | Full balance withdrawal down to exact Rs. 0.00 succeeds without rounding errors. | **PASS** |
| **9** | `testAccountClosureInvariants` | Account with balance > 0 rejects closure; account with balance == 0 allows closure. | **PASS** |
| **10** | `testFrozenClosedRestrictions` | Frozen and closed accounts strictly reject deposits, withdrawals, and outgoing transfers. | **PASS** |
| **11** | `testAtomicTransferAndFees` | Dual account balance updates, 0.1% fee deduction, and balancing dual ledger entries. | **PASS** |
| **12** | `testTransferIdempotencyKey` | Reused idempotency keys are caught and rejected to prevent duplicate debiting. | **PASS** |
| **13** | `testBeneficiaryDirectory` | Beneficiary registration, routing number validation, and directory lookup verified. | **PASS** |
| **14** | `testFixedDepositMaturity` | 6.5% simple annual interest and maturity calculation yield exact expected value. | **PASS** |
| **15** | `testLoanAmortizationRepayment` | Monthly EMI calculation (9.0% p.a.) and principal balance reduction verified. | **PASS** |
| **16** | `testRiskAlertSurveillance` | Transfers $\ge$ Rs. 45,000.00 flag `RiskAlert`; investigator resolution sets `RESOLVED`. | **PASS** |
| **17** | `testTrialBalanceReconciliation` | Total ledger journal debits strictly equal total credits with exactly 0.00 variance. | **PASS** |
| **18** | `testStatementReconciliation` | Opening balance + successful credits - successful debits = closing balance. | **PASS** |
| **19** | `testPersistenceAcrossRestart` | State saved to file, reloaded in a fresh database instance, and verified for exact fidelity. | **PASS** |
| **20** | `testCsvFormulaInjectionEscaping` | Prefixing `=, +, -, @` with `'` verified along with physical CSV file generation. | **PASS** |

---

## 13. Compilation and Execution Guide

### Option 1: Run the Automated Test Suite (Windows PowerShell)
```powershell
cd d:\kiran-academy-java-internship\October\Day03_Task3
javac -d . BankingApp.java BankingAppTest.java
java -cp . BankingAppTest
```

### Option 2: Run the Interactive Console Application
```powershell
cd d:\kiran-academy-java-internship\October\Day03_Task3
javac -d . BankingApp.java
java -cp . BankingApp
```

### Option 3: Standard Linux / Bash
```bash
cd October/Day03_Task3
javac -d . BankingApp.java BankingAppTest.java
java -cp . BankingAppTest
```

### Option 4: Continuous Integration
A pre-configured GitHub Actions CI workflow is available at [`.github/workflows/ci.yml`](file:///d:/kiran-academy-java-internship/.github/workflows/ci.yml) to automatically compile, verify, and test every push and pull request against JDK 17.

---

## 14. Educational Scope & Limitations Disclosure

1. **Educational Simulation**: Built as an advanced demonstration of banking engineering. Real-world commercial banks require Hardware Security Modules (HSMs), biometric/TOTP multi-factor authentication, distributed core-banking clusters (e.g. Temenos, Finacle), and regulatory oversight (RBI/Basel III).
2. **Persistence Mechanism**: Uses a robust local object-serialized state journal with atomic file writes. Production banking environments require distributed ACID RDBMS (PostgreSQL, Oracle) with synchronous write-ahead logging (WAL).
