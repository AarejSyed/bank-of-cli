package com.bankofcli.api;

import com.bankofcli.service.BankService;
import com.bankofcli.domain.BankTransaction;
import com.bankofcli.domain.BankTransactionType;

import java.util.Scanner;
import java.util.List;
import java.math.BigDecimal;

public class BankRepl {
    private BankService bankService;
    private Scanner console;
    private boolean isRunning;

    // Constructor: requires Bank Service layer
    public BankRepl(BankService bankService) {
        this.bankService = bankService;
        this.console = new Scanner(System.in);
        this.isRunning = false;
    }

    // Runs Bank of CLI program
    public void run() {
        isRunning = true;
        
        welcome();

        // REPL loop
        while (isRunning) {
            // If user is not logged in, we let user register account or log in
            if (!bankService.isLoggedIn()) { registerOrLogIn(); }

            // If user is logged in, we let them take actions on their account
            else { takeAccountAction(); }
        }
    }

    // Welcome user to program
    private void welcome() {
        System.out.println("WELCOME TO BANK OF CLI");
        System.out.println("\"Minimal interface, maximal customer service!\"");
        System.out.println();
    }

    // Prompt user to exit program, register, or log in
    private void registerOrLogIn() {
        // Available commands
        List<String> commands = List.of(
            "Exit Program",
            "Register Account",
            "Log In to Account"
        );

        int command = getValidIntCommandFromUser(commands);

        // Execute command
        switch (command) {
            case 0:
                stopBankRepl();
                break;
            case 1:
                registerAccount();
                break;
            case 2:
                logInToAccount();
                break;
            default:
                System.out.println("Input must be a valid command.");
                System.out.println();
        }
    }

    // Register new account
    private void registerAccount() {
        // Get PIN from user for new account
        System.out.println("Provide a 4-digit PIN for your new account:");
        String pin = userInput();
        
        // Register account and print ID
        try {
            long registeredAccountId = bankService.registerAccount(pin);
            System.out.println("Registered account! Log in with your ID: " + registeredAccountId);
        }

        // If account registry failed, let user know
        catch (Exception e) {
            System.out.println("Could not register account: " + e.getMessage());
        }
        
        System.out.println();
    }

    // Log in to existing account
    private void logInToAccount() {
        // Get account ID and PIN from user
        System.out.println("Account ID:");
        long accountId = Long.parseLong(userInput());
        System.out.println("PIN:");
        String pin = userInput();

        // Log in to account
        try {
            boolean loggedIn = bankService.logInToAccount(accountId, pin);
            if (loggedIn) { System.out.println("Successfully logged in!"); }
            else { System.out.println("Incorrect account ID or PIN."); }
        }

        // Account does not exist
        catch (Exception e) {
            System.out.println("Incorrect account ID or PIN.");
        }

        System.out.println();
    }

    // Prompt user to take action on their account
    private void takeAccountAction() {
        // Available commands
        List<String> commands = List.of(
            "Log Out of Account",
            "Check Account Balance",
            "Deposit Money",
            "Withdraw Money",
            "Transfer Money",
            "View Transaction History"
        );

        int command = getValidIntCommandFromUser(commands);

        // Execute command
        switch (command) {
            case 0:
                bankService.logOutOfAccount();
                System.out.println("Successfully logged out!");
                System.out.println();
                break;
            case 1:
                System.out.println("Account balance: $" + bigDecimalToString(bankService.getAccountBalance()));
                System.out.println();
                break;
            case 2:
                deposit();
                break;
            case 3:
                withdraw();
                break;
            case 4:
                transfer();
                break;
            case 5:
                viewTransactionHistory();
                break;
            default:
                System.out.println("Input must be a valid command.");
                System.out.println();
        }
    }

    // Display transaction history for logged in account
    private void viewTransactionHistory() {
        List<BankTransaction> transactionHistory = bankService.getBankTransactionHistory();

        // If no transactions, let user know
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions have been made on this account.");
            System.out.println();
            return;
        }

        // Print header
        System.out.println("ID\tACCOUNT\tTYPE\t\tAMOUNT\t\tTIMESTAMP");

        // Print every transaction
        for (BankTransaction transaction : transactionHistory) {
            System.out.println(transaction.getId() + "\t" +
                               transaction.getAccountId() + "\t" +
                               transaction.getType() + "\t" + (transaction.getType() == BankTransactionType.DEPOSIT ? "\t" : "") +
                               "$" + bigDecimalToString(transaction.getAmount()) + "\t\t" +
                               transaction.getTimestamp());
        }
        System.out.println();
    }

    // Prompt user to transfer money from their account to another account
    private void transfer() {
        // Get destination account ID String from user
        System.out.println("Account ID to transfer money to:");
        String destinationAccountIdString = userInput();

        // Convert destination account ID String to long
        long destinationAccountId;
        try {
            destinationAccountId = Long.parseLong(destinationAccountIdString);
        }
        catch (NumberFormatException e) {
            System.out.println("Input must be a valid integer.");
            System.out.println();
            return;
        }

        // If destination account ID is the same as logged in account ID, end transfer process
        if (destinationAccountId == bankService.getAccountId()) {
            System.out.println("Cannot transfer money to the same account.");
            System.out.println();
            return;
        }

        // If destination account ID does not exist, end transfer process
        if (!bankService.accountExists(destinationAccountId)) {
            System.out.println("Cannot transfer money to non-existent account " + destinationAccountId);
            System.out.println();
            return;
        }

        // Get amount of money to transfer from user
        BigDecimal amount = getAmountFromUser("transfer");

        // If amount is null, end transfer process
        if (amount == null) { return; }

        // If amount is greater than account balance, end transfer process
        if (amount.compareTo(bankService.getAccountBalance()) > 0) {
            System.out.println("Amount of money transferred cannot exceed account balance.");
            System.out.println();
            return;
        }

        // If amount is greater than $9999999.99, end transfer process
        if (amount.compareTo(new BigDecimal("9999999.99")) > 0) {
            System.out.println("Amount of money transferred cannot exceed $9999999.99.");
            System.out.println();
            return;
        }

        // Transfer money from account
        try {
            bankService.transfer(amount, destinationAccountId);
            System.out.println("Successfully transferred $" + bigDecimalToString(amount) + "!");
        }

        // If transfer failed, let user know
        catch (Exception e) {
            System.out.println("Could not transfer money: " + e.getMessage());
        }

        System.out.println();
    }

    // Prompt user to deposit money into their account
    private void deposit() {
        // Get amount of money to deposit from user
        BigDecimal amount = getAmountFromUser("deposit");

        // If amount is null, end deposit process
        if (amount == null) { return; }

        // If amount is greater than $9999999.99, end deposit process
        if (amount.compareTo(new BigDecimal("9999999.99")) > 0) {
            System.out.println("Amount of money deposited cannot exceed $9999999.99.");
            System.out.println();
            return;
        }

        // Deposit money into account
        try {
            bankService.deposit(amount);
            System.out.println("Successfully deposited $" + bigDecimalToString(amount) + "!");
        }

        // If deposit failed, let user know
        catch (Exception e) {
            System.out.println("Could not deposit money: " + e.getMessage());
        }

        System.out.println();
    }

    // Prompt user to withdraw money from their account
    private void withdraw() {
        // Get amount of money to withdraw from user
        BigDecimal amount = getAmountFromUser("withdraw");

        // If amount is null, end withdrawal process
        if (amount == null) { return; }

        // If amount is greater than account balance, end withdrawal process
        if (amount.compareTo(bankService.getAccountBalance()) > 0) {
            System.out.println("Amount of money withdrawn cannot exceed account balance.");
            System.out.println();
            return;
        }

        // If amount is greater than $9999999.99, end withdrawal process
        if (amount.compareTo(new BigDecimal("9999999.99")) > 0) {
            System.out.println("Amount of money withdrawn cannot exceed $9999999.99.");
            System.out.println();
            return;
        }

        // Withdraw money from account
        try {
            bankService.withdraw(amount);
            System.out.println("Successfully withdrew $" + bigDecimalToString(amount) + "!");
        }

        // If withdrawal failed, let user know
        catch (Exception e) {
            System.out.println("Could not withdraw money: " + e.getMessage());
        }

        System.out.println();
    }

    // Get amount of money from user
    private BigDecimal getAmountFromUser(String action) {
        System.out.println("Amount to " + action + ":");
        String amountString = userInput();

        // Convert amount to BigDecimal
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountString);
        }

        // Notify user and end process if amount is not a valid number
        catch (NumberFormatException e) {
            System.out.println("Input must be a valid number of format XXX.XX.");
            System.out.println();
            return null;
        }

        // Notify user and end process if amount is not positive or has more than 2 decimal places
        if (amount.signum() <= 0 || amount.scale() > 2) {
            System.out.println("Amount of money must be positive and must have at most 2 decimal places.");
            System.out.println();
            return null;
        }

        return amount;
    }
    
    // Get valid integer command from user
    // Verifies input is integer and that it corresponds to one of the provided commands
    private int getValidIntCommandFromUser(List<String> commands) {
        int command;
        
        // Get valid user command
        while (true) {
            printCommands(commands);
            
            // Get command
            try {
                command = Integer.parseInt(userInput());
            }

            // Ensure command is integer
            catch (NumberFormatException e) {
                System.out.println("Command input must be an integer.");
                System.out.println();
                continue;
            }

            // Ensure command is valid
            if (command >= 0 && command < commands.size()) { break; }
            else {
                System.out.println("Command input must be a valid command.");
                System.out.println();
            }
        }

        return command;
    } 
    
    // Receive user input
    private String userInput() {
        System.out.print("> ");
        String input = console.nextLine();
        System.out.println();
        return input;
    }

    // Print list of commands and their numbers
    private void printCommands(List<String> commands) {
        // Heading
        System.out.println("#\tCOMMAND");

        // Print every line
        for (int i = 0; i < commands.size(); i++) {
            System.out.println(i + "\t" + commands.get(i));
        }

        System.out.println();
    }

    // Convert BigDecimal to String with 2 decimal places
    private String bigDecimalToString(BigDecimal amount) {
        return String.format("%.2f", amount);
    }
    
    // Stop bank REPL
    private void stopBankRepl() {
        console.close();
        System.out.println("Thank you for banking with Bank of CLI!");
        System.out.println();
        isRunning = false;
    }
}
