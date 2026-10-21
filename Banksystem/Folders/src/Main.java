import java.util.Scanner;
import menu.MainMenu;
import persistence.BankFileManager;

public class Main {

    public static void main(String[] args) {
        BankFileManager.load();
        Runtime.getRuntime().addShutdownHook(new Thread(BankFileManager::save));
        try (Scanner scanner = new Scanner(System.in)) {
            new MainMenu(scanner).start();
        } finally {
            BankFileManager.save();
        }
    }
}
