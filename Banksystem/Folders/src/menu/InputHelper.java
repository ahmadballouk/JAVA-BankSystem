package menu;

import java.util.Scanner;
import validation.InputValidator;

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public double amount(String prompt) {
        while (true) {
            try {
                double value = Double.parseDouble(text(prompt));
                if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
                return value;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Enter a valid non-negative amount.");
            }
        }
    }

    public int integer(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(text(prompt));
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Enter a valid whole number.");
            }
        }
    }

    public double positiveAmount(String prompt) {
        while (true) {
            double value = amount(prompt);
            if (value > 0) return value;
            System.out.println("ERROR: Amount must be greater than zero.");
        }
    }

    public int positiveInteger(String prompt) {
        while (true) {
            int value = integer(prompt);
            if (value > 0) return value;
            System.out.println("ERROR: Enter a number greater than zero.");
        }
    }

    public String name(String prompt) {
        while (true) {
            String value = text(prompt);
            if (InputValidator.isValidName(value)) return value;
            System.out.println("ERROR: Enter a valid name using letters, spaces, apostrophes, dots or hyphens only.");
        }
    }

    public String phoneNumber(String prompt) {
        while (true) {
            String value = text(prompt);
            if (InputValidator.isValidPhoneNumber(value)) return value;
            System.out.println("ERROR: Enter a valid phone number (7–15 digits, optional +, spaces, - or parentheses).");
        }
    }

    public String optionalPhoneNumber(String prompt) {
        while (true) {
            String value = optionalText(prompt);
            if (value.isBlank() || InputValidator.isValidPhoneNumber(value)) return value;
            System.out.println("ERROR: Enter a valid phone number or leave it empty.");
        }
    }

    public String email(String prompt) {
        while (true) {
            String value = text(prompt);
            if (InputValidator.isValidEmail(value)) return value;
            System.out.println("ERROR: Enter a valid email address.");
        }
    }

    public String optionalEmail(String prompt) {
        while (true) {
            String value = optionalText(prompt);
            if (value.isBlank() || InputValidator.isValidEmail(value)) return value;
            System.out.println("ERROR: Enter a valid email address or leave it empty.");
        }
    }

    public String optionalText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String text(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("ERROR: Input cannot be empty.");
        }
    }

    public boolean yesNo(String prompt) {
        while (true) {
            String value = text(prompt).toLowerCase();
            if (value.equals("y") || value.equals("yes")) return true;
            if (value.equals("n") || value.equals("no")) return false;
            System.out.println("ERROR: Enter Y or N.");
        }
    }

    public void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
