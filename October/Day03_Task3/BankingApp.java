import java.util.Scanner;

public class BankingApp {
    public static void main(String[] args) {
        String bankName = "Kiran Bank";
        String customerName = "Demo Customer";
        String accountNumber = "KBA100001";
        String accountType = "Savings Account";
        double openingBalance = 10000.00;
        double balance = 10000.00;
        String demoPin = "1234";

        int maxTransactions = 1000;
        String[] txnIds = new String[maxTransactions];
        String[] txnTypes = new String[maxTransactions];
        double[] txnAmounts = new double[maxTransactions];
        double[] txnBalances = new double[maxTransactions];
        String[] txnStatuses = new String[maxTransactions];
        int transactionCount = 0;
        int nextTxnId = 1001;

        double totalDeposits = 0.00;
        double totalWithdrawals = 0.00;

        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("       Welcome to Kiran Bank");
        System.out.println("   Console Banking Application v2.0");
        System.out.println("========================================");
        System.out.println("[Demonstration Mode] Demo PIN: 1234");

        int attempts = 0;
        boolean authenticated = false;

        while (attempts < 3) {
            System.out.print("Enter 4-digit PIN: ");
            String enteredPin = scanner.nextLine();
            if (enteredPin.equals(demoPin)) {
                authenticated = true;
                break;
            } else {
                attempts++;
                int remaining = 3 - attempts;
                if (remaining > 0) {
                    System.out.printf("Incorrect PIN. Remaining attempts: %d%n", remaining);
                } else {
                    System.out.println("Incorrect PIN. Maximum attempts exceeded. Access denied.");
                }
            }
        }

        if (!authenticated) {
            scanner.close();
            return;
        }

        while (true) {
            System.out.println("========================================");
            System.out.println("              MAIN MENU");
            System.out.println("========================================");
            System.out.println("1. View Account Details");
            System.out.println("2. Check Balance");
            System.out.println("3. Deposit Money");
            System.out.println("4. Withdraw Money");
            System.out.println("5. View Transaction History");
            System.out.println("6. View Account Statement");
            System.out.println("7. Search Transaction by ID");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.println("--------- Account Details ---------");
                System.out.println("Bank Name: " + bankName);
                System.out.println("Customer Name: " + customerName);
                System.out.println("Account Number: " + accountNumber);
                System.out.println("Account Type: " + accountType);
                System.out.printf("Current Balance: Rs. %.2f%n", balance);
                System.out.println("-----------------------------------");
            } else if (choice.equals("2")) {
                System.out.printf("Current Balance: Rs. %.2f%n", balance);
            } else if (choice.equals("3")) {
                System.out.print("Enter deposit amount: ");
                String input = scanner.nextLine();
                try {
                    double amount = Double.parseDouble(input);
                    if (!Double.isFinite(amount)) {
                        System.out.println("Invalid input. Please enter a numeric amount.");
                    } else if (amount <= 0) {
                        System.out.println("Invalid amount. Deposit amount must be greater than 0.");
                    } else if (!Double.isFinite(balance + amount)) {
                        System.out.println("Transaction failed: Deposit amount exceeds maximum limit.");
                    } else {
                        balance += amount;
                        totalDeposits += amount;
                        String currentTxnId = "TXN" + nextTxnId;
                        nextTxnId++;
                        txnIds[transactionCount] = currentTxnId;
                        txnTypes[transactionCount] = "DEPOSIT";
                        txnAmounts[transactionCount] = amount;
                        txnBalances[transactionCount] = balance;
                        txnStatuses[transactionCount] = "SUCCESS";
                        transactionCount++;

                        System.out.println("--- Transaction Receipt ---");
                        System.out.println("Transaction ID: " + currentTxnId);
                        System.out.println("Transaction Type: DEPOSIT");
                        System.out.printf("Amount: Rs. %.2f%n", amount);
                        System.out.printf("Balance: Rs. %.2f%n", balance);
                        System.out.println("Status: SUCCESS");
                        System.out.println("---------------------------");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a numeric amount.");
                }
            } else if (choice.equals("4")) {
                System.out.print("Enter withdrawal amount: ");
                String input = scanner.nextLine();
                try {
                    double amount = Double.parseDouble(input);
                    if (!Double.isFinite(amount)) {
                        System.out.println("Invalid input. Please enter a numeric amount.");
                    } else if (amount <= 0) {
                        System.out.println("Invalid amount. Withdrawal amount must be greater than 0.");
                    } else if (amount > balance) {
                        System.out.printf("Transaction failed: Insufficient balance. Current Balance: Rs. %.2f%n", balance);
                    } else {
                        balance -= amount;
                        totalWithdrawals += amount;
                        String currentTxnId = "TXN" + nextTxnId;
                        nextTxnId++;
                        txnIds[transactionCount] = currentTxnId;
                        txnTypes[transactionCount] = "WITHDRAWAL";
                        txnAmounts[transactionCount] = amount;
                        txnBalances[transactionCount] = balance;
                        txnStatuses[transactionCount] = "SUCCESS";
                        transactionCount++;

                        System.out.println("--- Transaction Receipt ---");
                        System.out.println("Transaction ID: " + currentTxnId);
                        System.out.println("Transaction Type: WITHDRAWAL");
                        System.out.printf("Amount: Rs. %.2f%n", amount);
                        System.out.printf("Balance: Rs. %.2f%n", balance);
                        System.out.println("Status: SUCCESS");
                        System.out.println("---------------------------");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a numeric amount.");
                }
            } else if (choice.equals("5")) {
                if (transactionCount == 0) {
                    System.out.println("No transactions available.");
                } else {
                    System.out.println("----------------- Transaction History -----------------");
                    int i = 0;
                    while (i < transactionCount) {
                        System.out.printf("Transaction ID: %s | Type: %-10s | Amount: Rs. %10.2f | Balance: Rs. %10.2f | Status: %s%n",
                                txnIds[i], txnTypes[i], txnAmounts[i], txnBalances[i], txnStatuses[i]);
                        i++;
                    }
                    System.out.println("-------------------------------------------------------");
                }
            } else if (choice.equals("6")) {
                System.out.println("--------------- Account Statement ---------------");
                System.out.println("Account Number: " + accountNumber);
                System.out.printf("Opening Balance: Rs. %.2f%n", openingBalance);
                System.out.printf("Total Successful Deposits: Rs. %.2f%n", totalDeposits);
                System.out.printf("Total Successful Withdrawals: Rs. %.2f%n", totalWithdrawals);
                System.out.printf("Number of Successful Transactions: %d%n", transactionCount);
                System.out.printf("Current Balance: Rs. %.2f%n", balance);
                System.out.println("-------------------------------------------------");
            } else if (choice.equals("7")) {
                System.out.print("Enter transaction ID: ");
                String searchId = scanner.nextLine();
                boolean found = false;
                int i = 0;
                while (i < transactionCount) {
                    if (txnIds[i].equalsIgnoreCase(searchId.trim())) {
                        System.out.println("--- Transaction Receipt ---");
                        System.out.println("Transaction ID: " + txnIds[i]);
                        System.out.println("Transaction Type: " + txnTypes[i]);
                        System.out.printf("Amount: Rs. %.2f%n", txnAmounts[i]);
                        System.out.printf("Balance: Rs. %.2f%n", txnBalances[i]);
                        System.out.println("Status: " + txnStatuses[i]);
                        System.out.println("---------------------------");
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found) {
                    System.out.println("Transaction not found.");
                }
            } else if (choice.equals("8")) {
                System.out.print("Are you sure you want to exit? (yes/no): ");
                String confirm = scanner.nextLine();
                if (confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Thank you for banking with us. Goodbye!");
                    break;
                } else if (confirm.equalsIgnoreCase("no")) {
                    System.out.println("Exit cancelled. Returning to main menu.");
                } else {
                    System.out.println("Invalid response. Returning to main menu.");
                }
            } else {
                System.out.println("Invalid choice. Please enter a number from 1 to 8.");
            }
        }

        scanner.close();
    }
}
