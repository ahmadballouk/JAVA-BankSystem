package menu;

import persistence.BankFileManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import model.account.Account;
import model.bank.MonthlyReport;
import model.customer.Customer;
import service.GlobalData;
import service.month.EndOfMonthProcessor;
import service.report.ReportService;

public class ReportMenu {

    private final InputHelper input;
    private final EndOfMonthProcessor endOfMonthProcessor;
    private final AccountSelectionHelper accountSelector;
    private final CustomerSelectionHelper customerSelector;

    public ReportMenu(Scanner scanner) {
        this.input = new InputHelper(scanner);
        this.endOfMonthProcessor = new EndOfMonthProcessor();
        this.accountSelector = new AccountSelectionHelper(scanner);
        this.customerSelector = new CustomerSelectionHelper(scanner);
    }

    public void start() {
        boolean back = false;

        while (!back) {
            MainMenu.header("REPORTS & END OF MONTH");
            System.out.println("1. Customer Report");
            System.out.println("2. Account Transaction Report");
            System.out.println("3. Current Month Activity");
            System.out.println("4. Stored Monthly Reports");
            System.out.println("5. Run End-of-Month Processing");
            System.out.println("6. Bank Asset Summary");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> customerReport();
                case 2 -> accountTransactionReport();
                case 3 -> ReportService.currentMonthReport();
                case 4 -> monthlyReports();
                case 5 -> runEndOfMonth();
                case 6 -> GlobalData.showBankAssets();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();

            if (!back) {
                input.pause();
            }
        }
    }

    private void accountTransactionReport() {
        Account account = accountSelector.selectAccount();
        if (account == null) {
            return;
        }

        String start = input.text("Start date (YYYY-MM-DD): ");
        String end = input.text("End date (YYYY-MM-DD): ");
        ReportService.rangeTransactionAccountReport(account, start, end);
    }

    private void customerReport() {
        Customer customer = customerSelector.selectCustomer();
        if (customer != null) {
            ReportService.customerReport(customer);
        }
    }

    private void monthlyReports() {
        List<MonthlyReport> reports = new ArrayList<>(GlobalData.getMonthlyReports().values());

        if (reports.isEmpty()) {
            System.out.println("No monthly reports are available.");
            return;
        }

        reports.sort(Comparator.comparing(MonthlyReport::getMonth).reversed());

        for (int i = 0; i < reports.size(); i++) {
            MonthlyReport report = reports.get(i);
            System.out.printf("%d. %s%n", i + 1, report.getMonth());
        }

        int choice = input.integer("Report number: ");
        if (choice < 1 || choice > reports.size()) {
            System.out.println("Invalid report selection.");
            return;
        }

        reports.get(choice - 1).displayReport();
    }

    private void runEndOfMonth() {
        if (!input.yesNo("Advance the banking date by one month and process all accounts? [Y/N]: ")) {
            return;
        }

        endOfMonthProcessor.executeEndOfMonth();
    }

}
