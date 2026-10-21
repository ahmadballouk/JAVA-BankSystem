package menu;

import persistence.BankFileManager;
import java.util.Scanner;
import repository.AccountRepository;

public class AccountMenu {

    private final InputHelper input;
    private final AccountRepository repository;

    public AccountMenu(Scanner scanner) {
        input = new InputHelper(scanner);
        repository = new AccountRepository(scanner);
    }

    public void start() {
        boolean back = false;
        while (!back) {
            MainMenu.header("ACCOUNT MANAGEMENT");
            System.out.println("1. Open Account");
            System.out.println("2. View All Accounts");
            System.out.println("3. View Account Details");
            System.out.println("4. Close Account");
            System.out.println("5. Freeze Account");
            System.out.println("6. Activate Frozen Account by Deposit");
            System.out.println("7. Activate Frozen Account by Transfer");
            System.out.println("8. Fixed Deposit Details");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> repository.openAccount();
                case 2 -> repository.viewAllAccounts();
                case 3 -> repository.viewAccountDetails();
                case 4 -> repository.closeAccount();
                case 5 -> repository.freezeAccount();
                case 6 -> repository.activateByDeposit();
                case 7 -> repository.activateByTransfer();
                case 8 -> repository.fixedDepositDetails();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();
            if (!back) input.pause();
        }
    }
}
