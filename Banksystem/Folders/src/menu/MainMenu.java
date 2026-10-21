package menu;

import java.util.Scanner;
import persistence.BankFileManager;

public class MainMenu {

    private static final String LINE = "============================================================";

    private final InputHelper input;
    private final CustomerMenu customerMenu;
    private final AccountMenu accountMenu;
    private final TransactionMenu transactionMenu;
    private final StatisticsMenu statisticsMenu;
    private final ReportMenu reportMenu;
    private final AdvancedSearchMenu advancedSearchMenu;

    public MainMenu(Scanner scanner) {
        input = new InputHelper(scanner);
        customerMenu = new CustomerMenu(scanner);
        accountMenu = new AccountMenu(scanner);
        transactionMenu = new TransactionMenu(scanner);
        statisticsMenu = new StatisticsMenu(scanner);
        reportMenu = new ReportMenu(scanner);
        advancedSearchMenu = new AdvancedSearchMenu(scanner);
    }

    public static void header(String title) {
        System.out.println("\n" + LINE);
        System.out.printf("%" + ((60 + title.length()) / 2) + "s%n", title);
        System.out.println(LINE);
    }

    public void start() {
        boolean running = true;
        while (running) {
            header("BANK MANAGEMENT SYSTEM");
            System.out.println("1. Customer Management");
            System.out.println("2. Account Management");
            System.out.println("3. Transaction Management");
            System.out.println("4. Bank Statistics");
            System.out.println("5. Reports & End of Month");
            System.out.println("6. Advanced Search");
            System.out.println("0. Exit");

            switch (input.integer("Select an option: ")) {
                case 1 -> customerMenu.start();
                case 2 -> accountMenu.start();
                case 3 -> transactionMenu.start();
                case 4 -> statisticsMenu.start();
                case 5 -> reportMenu.start();
                case 6 -> advancedSearchMenu.start();
                case 0 -> {
                    running = false;
                    System.out.println("\nThank you for using the Bank Management System.");
                }
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();
        }
    }
}
