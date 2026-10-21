package menu;

import persistence.BankFileManager;
import java.util.Scanner;

public class TransactionMenu {

    private final InputHelper input;
    private final TransactionRepository transactionRepository;

    public TransactionMenu(Scanner scanner) {
        input = new InputHelper(scanner);
        transactionRepository = new TransactionRepository(scanner);
    }

    public void start() {
        boolean back = false;
        while (!back) {
            MainMenu.header("TRANSACTION MANAGEMENT");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. View Transaction Info");
            System.out.println("5. Account Transaction History");
            System.out.println("6. View All Transactions");
            System.out.println("7. View Transactions by Status, Type or Date Range");
            System.out.println("8. View Paused Transactions");
            System.out.println("9. Resume Paused Transaction");
            System.out.println("10. Resolve Orphaned Paused Transactions");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> transactionRepository.deposit();
                case 2 -> transactionRepository.withdraw();
                case 3 -> transactionRepository.transfer();
                case 4 -> transactionRepository.viewTransactionInfo();
                case 5 -> transactionRepository.viewAccountTransactionHistory();
                case 6 -> transactionRepository.displayAllTransactions();
                case 7 -> transactionRepository.viewFilteredTransactions();
                case 8 -> transactionRepository.pausedTransactionsMenu();
                case 9 -> transactionRepository.resumePausedTransaction();
                case 10 -> transactionRepository.resolveOrphanedPausedTransactions();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();
            if (!back) input.pause();
        }
    }
}
