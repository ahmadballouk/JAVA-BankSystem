package service.month;

import model.account.Account;
import model.bank.MonthlyReport;
import model.customer.Customer;
import service.GlobalData;

public class EndOfMonthProcessor {

    public void executeEndOfMonth() {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   STARTING END-OF-MONTH PROCESSING    ║");
        System.out.println("╚════════════════════════════════════════╝");

        util.DateTimeManager.advanceOneMonth();
        System.out.println("Banking date advanced to: " + util.DateTimeManager.getCurrentDate());

        processAllAccounts();

        createMonthlyReport();

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   END-OF-MONTH PROCESSING COMPLETE    ║");
        System.out.println("╚════════════════════════════════════════╝\n");

    }

   public  void createMonthlyReport ()
   {

      MonthlyReport monthlyReport = new MonthlyReport(GlobalData.getMonthlyCustomersAdded(),
              GlobalData.getMonthlyAccountsOpened(), GlobalData.getMonthlyTransactionsCount(), GlobalData.getMonthlyDepositsAmount(),
              GlobalData.getMonthlyWithdrawalsAmount(), GlobalData.getMonthlyFeesCharged(), GlobalData.getMonthlyInterestPaid(),
              getCustomerWithHighestBalance());

              GlobalData.resetMonthlyData();
   }

    public void processAllAccounts() {
        System.out.println("\n========== PROCESSING ALL ACCOUNTS ==========");

        int accountsProcessed = 0;

        for (Customer customer : GlobalData.getAllCustomers()) {
            for (Account account : customer.getAccounts()) {
                account.processEndOfMonth();
                accountsProcessed++;
            }
        }

        System.out.println("Total Accounts Processed: " + accountsProcessed);
        System.out.println("============================================\n");

    }

    public Customer getCustomerWithHighestBalance() {
        Customer highest = null;
        double maxBalance = -Double.MAX_VALUE;

        for (Customer customer : GlobalData.getAllCustomers()) {
            double total = customer.getTotalBalance();
            if (total > maxBalance) {
                maxBalance = total;
                highest = customer;
            }
        }

        return highest;
    }

}
