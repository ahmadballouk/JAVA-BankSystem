package persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import model.bank.MonthlyReport;
import service.GlobalData;
import util.DateTimeManager;

public final class BankFileManager {

    private static final Path DATA_FOLDER = Path.of("data");

    private static final Path STATE_FILE = DATA_FOLDER.resolve("bank_state.dat");
    private static final Path COUNTERS_FILE = DATA_FOLDER.resolve("counters.txt");
    private static final Path MONTHLY_REPORTS_FILE = DATA_FOLDER.resolve("monthly_reports.txt");
    private static final Path ERROR_FILE = DATA_FOLDER.resolve("errors.log");


    private BankFileManager() {
    }

    

    public static void load() {
        try {
            Files.createDirectories(DATA_FOLDER);
            Path temporaryFile = DATA_FOLDER.resolve("bank_state.tmp");
            if (!Files.exists(STATE_FILE) && Files.exists(temporaryFile)) {
                try {
                    try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(temporaryFile))) {
                        Object saved = input.readObject();
                        if (!(saved instanceof GlobalData.Snapshot)) {
                            throw new IOException("Temporary bank state has an unknown format.");
                        }
                    }
                    moveStateFile(temporaryFile, STATE_FILE);
                    System.out.println("Recovered bank data from the temporary save file.");
                } catch (Exception recoveryFailure) {
                    if (Files.exists(COUNTERS_FILE) || Files.exists(MONTHLY_REPORTS_FILE)) {
                        throw new IOException("The main bank state is missing and the temporary recovery file "
                                + "could not be restored. Existing saved data was preserved.", recoveryFailure);
                    }
                    Files.deleteIfExists(temporaryFile);
                }
            }

            if (!Files.exists(STATE_FILE)
                    && (Files.exists(COUNTERS_FILE) || Files.exists(MONTHLY_REPORTS_FILE))) {
                throw new IOException("The main bank state file is missing while other saved-data files remain. "
                        + "Startup was stopped to avoid silently initializing an empty bank.");
            }

            createTextFile(COUNTERS_FILE, "customerCounter=0\naccountCounter=0\ntransactionCounter=1\n");
            createTextFile(MONTHLY_REPORTS_FILE, "month|customersAdded|accountsOpened|transactions|deposits|withdrawals|fees|interest\n");
            createTextFile(ERROR_FILE, "");

            if (!Files.exists(STATE_FILE)) {
                DateTimeManager.setCurrentDate(LocalDate.now());
                if (!save()) {
                    throw new IOException("Could not create the initial bank data file.");
                }
                System.out.println("No previous bank data found. A new data store was created.");
                return;
            }

            try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(STATE_FILE))) {
                Object saved = input.readObject();
                if (!(saved instanceof GlobalData.Snapshot snapshot)) {
                    throw new IOException("Saved bank data has an unknown format.");
                }
                GlobalData.restoreSnapshot(snapshot);
            }
            writeReadableFiles();
            System.out.printf("Loaded %d customers, %d accounts, %d transactions and %d monthly reports.%n",
                    GlobalData.getTotalCustomers(), GlobalData.getTotalAccounts(),
                    GlobalData.getTotalTransactions(), GlobalData.getMonthlyReports().size());
        } catch (Exception e) {
            logError("Startup load failed: " + e.getMessage());
            System.out.println("Could not load saved bank data. Details were written to data/errors.log.");
            throw new IllegalStateException("Bank data could not be loaded safely.", e);
        }
    }

    public static boolean save() {
        try {
            Files.createDirectories(DATA_FOLDER);
            Path temporaryFile = DATA_FOLDER.resolve("bank_state.tmp");
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporaryFile))) {
                output.writeObject(GlobalData.createSnapshot());
            }
            moveStateFile(temporaryFile, STATE_FILE);
            writeReadableFiles();
            return true;
        } catch (Exception e) {
            logError("Save failed: " + e.getMessage());
            System.out.println("WARNING: Bank data could not be saved. See data/errors.log.");
            return false;
        }
    }

    private static void moveStateFile(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException unsupportedAtomicMove) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void writeReadableFiles() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(COUNTERS_FILE, StandardCharsets.UTF_8)) {
            writer.write("customerCounter=" + model.customer.Customer.getCustomerCounter()); writer.newLine();
            writer.write("accountCounter=" + model.account.Account.getAccountCounter()); writer.newLine();
            writer.write("transactionCounter=" + model.transaction.Transaction.getTransactionCounter()); writer.newLine();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(MONTHLY_REPORTS_FILE, StandardCharsets.UTF_8)) {
            writer.write("month|customersAdded|accountsOpened|transactions|deposits|withdrawals|fees|interest");
            writer.newLine();
            for (Map.Entry<YearMonth, MonthlyReport> entry : GlobalData.getMonthlyReports().entrySet()) {
                MonthlyReport report = entry.getValue();
                writer.write(entry.getKey() + "|" + report.getMonthlyCustomersAdded() + "|"
                        + report.getMonthlyAccountsOpened() + "|" + report.getMonthlyTransactionsCount() + "|"
                        + report.getMonthlyDepositsAmount() + "|" + report.getMonthlyWithdrawalsAmount() + "|"
                        + report.getMonthlyFeesCharged() + "|" + report.getMonthlyInterestPaid());
                writer.newLine();
            }
        }
    }

    private static void createTextFile(Path path, String initialContent) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, initialContent, StandardCharsets.UTF_8);
        }
    }

    private static void logError(String message) {
        try {
            Files.createDirectories(DATA_FOLDER);
            Files.writeString(ERROR_FILE, java.time.LocalDateTime.now() + " | " + message + System.lineSeparator(),
                    StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            System.err.println(message);
        }
    }
}
