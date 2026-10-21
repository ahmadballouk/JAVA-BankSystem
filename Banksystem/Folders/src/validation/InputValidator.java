package validation;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern NAME = Pattern.compile("^[\\p{L}]+(?:[ .'-][\\p{L}]+)*$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()-]+$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private InputValidator() {
    }

    public static boolean isValidName(String value) {
        return value != null && NAME.matcher(value.trim()).matches();
    }

    public static boolean isValidPhoneNumber(String value) {
        if (value == null || !PHONE.matcher(value.trim()).matches()) return false;
        long digits = value.chars().filter(Character::isDigit).count();
        return digits >= 7 && digits <= 15;
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }
}
