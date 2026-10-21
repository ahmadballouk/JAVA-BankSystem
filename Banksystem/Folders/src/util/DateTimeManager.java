package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeManager {

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static void advanceOneMonth() {
        currentSystemDate = currentSystemDate.plusMonths(1);
    }

    public static String dateToString(LocalDateTime date) {
        if (date == null)
            return "";
        return date.format(FORMATTER);
    }

    public static LocalDate stringToDate(String dateStr) {
        return LocalDate.parse(dateStr.trim(), FORMATTER);
    }

    private static LocalDate currentSystemDate = LocalDate.now();

    public static LocalDate getToday() {
        return LocalDate.now();
    }

    public static LocalDate getCurrentDate() {
        return currentSystemDate;
    }

    public static void setCurrentDate(LocalDate date) {
        if (date != null) currentSystemDate = date;
    }

    public static int getCurrentMonthValue() {
        return currentSystemDate.getMonthValue();
    }

         public static String getFormattedDateTimeString() {
        LocalTime executionTime = LocalTime.now().withNano(0);

        LocalDateTime systemDateTime = LocalDateTime.of(currentSystemDate, executionTime);

        return systemDateTime.format(ISO_DATETIME_FORMATTER);
    }
}
