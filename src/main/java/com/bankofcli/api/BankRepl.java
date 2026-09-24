package com.bankofcli.api;

import com.bankofcli.service.BankService;
import java.util.Scanner;
import java.util.List;

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
        }
    }

    // Welcome user to program
    private void welcome() {
        System.out.println("WELCOME TO BANK OF CLI");
        System.out.println("\"Minimal interface, maximal customer service!\"");
        System.out.println();
    }

    // Offer that user can register or log in
    private void registerOrLogIn() {
        // Available commands
        List<String> commands = List.of(
            "Exit Program",
            "Register Account",
            "Log In to Account"
        );

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
                System.out.println("Input must be an integer.");
                System.out.println();
                continue;
            }

            // Ensure command is valid
            if (command >= 0 && command < commands.size()) { break; }
            else {
                System.out.println("Input must be a valid command.");
                System.out.println();
            }
        }

        // Execute command
        switch (command) {
            case 0:
                stopBankRepl();
                break;
            case 1:
                System.out.println("Registration coming soon!");
                System.out.println();
                break;
            case 2:
                System.out.println("Login coming soon!");
                System.out.println();
                break;
            default:
                System.out.println("Input must be a valid command.");
        }
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

    // Stop bank REPL
    private void stopBankRepl() {
        console.close();
        System.out.println("Thank you for banking with Bank of CLI!");
        System.out.println();
        isRunning = false;
    }
}
