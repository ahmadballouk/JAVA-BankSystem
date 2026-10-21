package model.bank;

import java.time.YearMonth;
import model.customer.Customer;
import service.GlobalData;
import util.DateTimeManager;

public class MonthlyReport implements java.io.Serializable {

    private double totalInterestPaid = 0;
    private double totalFeeCharged = 0;
    private double totalDepositsAmount = 0;
    private double totalWithdrawalsAmount = 0;

    private YearMonth month;
    private int monthlyCustomersAdded;
    private int monthlyAccountsOpened;
    private int monthlyTransactionsCount;
    private double monthlyDepositsAmount;
    private double monthlyWithdrawalsAmount;
    private double monthlyFeesCharged;
    private double monthlyInterestPaid;
    private int totalCheckingAccounts;
    private int totalFixedDepositAccounts;
    private int totalSavingsAccounts;
    private int totalCustomers;
    private int totalAccounts;
    private int totalTransactions;
    private String customerWithHighestBalance;
    private double highestBalance;

    private YearMonth monthKey;

    public MonthlyReport(
            int monthlyCustomersAdded,
            int monthlyAccountsOpened,
            int monthlyTransactionsCount,
            double monthlyDepositsAmount,
            double monthlyWithdrawalsAmount,
            double monthlyFeesCharged,
            double monthlyInterestPaid,
            Customer customerWithHighestBalance) {

        this.monthKey = YearMonth.from(DateTimeManager.getCurrentDate());
        this.month = YearMonth.from(DateTimeManager.getCurrentDate());

        if (customerWithHighestBalance != null) {
            this.customerWithHighestBalance = customerWithHighestBalance.getFullName();
            this.highestBalance = customerWithHighestBalance.getTotalBalance();
        } else {
            this.customerWithHighestBalance = "N/A";
            this.highestBalance = 0.0;
        }

        this.monthlyCustomersAdded = monthlyCustomersAdded;
        this.monthlyAccountsOpened = monthlyAccountsOpened;
        this.monthlyTransactionsCount = monthlyTransactionsCount;
        this.monthlyDepositsAmount = monthlyDepositsAmount;
        this.monthlyWithdrawalsAmount = monthlyWithdrawalsAmount;
        this.monthlyFeesCharged = monthlyFeesCharged;
        this.monthlyInterestPaid = monthlyInterestPaid;

        this.totalAccounts = GlobalData.getTotalAccounts();
        this.totalCustomers = GlobalData.getTotalCustomers();
        this.totalTransactions = GlobalData.getTotalTransactions();

        this.totalCheckingAccounts = GlobalData.totalCheckingAccounts();
        this.totalFixedDepositAccounts = GlobalData.totalFixedDepositAccounts();
        this.totalSavingsAccounts = GlobalData.totalSavingsAccounts();

        totalDepositsAmount = GlobalData.getTotalDepositsAmount();
        totalWithdrawalsAmount = GlobalData.getTotalWithdrawalsAmount();
        totalFeeCharged = GlobalData.getTotalFeesCharged();
        totalInterestPaid = GlobalData.getTotalInterestPaid();

        GlobalData.getMonthlyReports().put(monthKey, this);
    }

    public int getCheckingAccounts() {
        return totalCheckingAccounts;
    }

    public String getCustomerWithHighestBalance() {
        return customerWithHighestBalance;
    }

    public double getHighestBalance() {
        return highestBalance;
    }

    public YearMonth getMonth() {
        return month;
    }

    public YearMonth getMonthKey() {
        return monthKey;
    }

    public int getMonthlyAccountsOpened() {
        return monthlyAccountsOpened;
    }

    public int getMonthlyCustomersAdded() {
        return monthlyCustomersAdded;
    }

    public double getMonthlyDepositsAmount() {
        return monthlyDepositsAmount;
    }

    public double getMonthlyFeesCharged() {
        return monthlyFeesCharged;
    }

    public double getMonthlyInterestPaid() {
        return monthlyInterestPaid;
    }

    public int getMonthlyTransactionsCount() {
        return monthlyTransactionsCount;
    }

    public double getMonthlyWithdrawalsAmount() {
        return monthlyWithdrawalsAmount;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public int getTotalFixedDepositAccounts() {
        return totalFixedDepositAccounts;
    }

    public int getTotalSavingsAccounts() {
        return totalSavingsAccounts;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void displayReport() {
        System.out.println("\n==================================================");
        System.out.println("            MONTHLY REPORT (Month: " + monthKey + ")");
        System.out.println("==================================================");

        System.out.println("\n--- HIGHEST BALANCE CUSTOMER ---");
        System.out.println(" Customer Name  : " + (customerWithHighestBalance != null ? customerWithHighestBalance : "N/A"));
        System.out.println(" Total Balance  : $" + String.format("%.2f", highestBalance >= 0 ? highestBalance : 0.0));

        System.out.println("\n--- MONTHLY ACTIVITY ---");
        System.out.println(" New Customers  : " + monthlyCustomersAdded);
        System.out.println(" Accounts Opened: " + monthlyAccountsOpened);
        System.out.println(" Transactions   : " + monthlyTransactionsCount);
        System.out.println(" Deposits       : $" + String.format("%.2f", monthlyDepositsAmount));
        System.out.println(" Withdrawals    : $" + String.format("%.2f", monthlyWithdrawalsAmount));
        System.out.println(" Fees Charged   : $" + String.format("%.2f", monthlyFeesCharged));
        System.out.println(" Interest Paid  : $" + String.format("%.2f", monthlyInterestPaid));

        System.out.println("\n--- BANK TOTALS SNAPSHOT ---");
        System.out.println(" Total Customers: " + totalCustomers);
        System.out.println(" Total Accounts : " + totalAccounts);
        System.out.println("   └─ Checking  : " + totalCheckingAccounts);
        System.out.println("   └─ Savings   : " + totalSavingsAccounts);
        System.out.println("   └─ Fixed Dep.: " + totalFixedDepositAccounts);
        System.out.println(" Total All-Time Transactions: " + totalTransactions);

        System.out.println("\n--- ALL-TIME FINANCIAL TOTALS ---");
        System.out.println(" Total Deposits    : $" + String.format("%.2f", totalDepositsAmount));
        System.out.println(" Total Withdrawals : $" + String.format("%.2f", totalWithdrawalsAmount));
        System.out.println(" Total Fees Charged: $" + String.format("%.2f", totalFeeCharged));
        System.out.println(" Total Interest    : $" + String.format("%.2f", totalInterestPaid));
        System.out.println("==================================================\n");
    }
}
