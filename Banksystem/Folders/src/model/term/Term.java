package model.term;

import java.time.LocalDate;
import util.DateTimeManager;

public class Term implements java.io.Serializable {

    private int totalMonths;
    private LocalDate startDate;
    private LocalDate maturityDate;

    public Term(int months) {

        this.totalMonths = months;
        this.startDate = DateTimeManager.getCurrentDate();
        this.maturityDate = this.startDate.plusMonths(months);
    }

    public void renew(int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Term months must be greater than 0");
        }
        this.totalMonths = months;
        this.startDate = DateTimeManager.getCurrentDate();
        this.maturityDate = this.startDate.plusMonths(months);
    }

    public LocalDate getMaturityDate() {

        return maturityDate;

    }

    public int getMonthsElapsed() {
        LocalDate start = this.startDate;
        LocalDate current = util.DateTimeManager.getCurrentDate();

        if (isMatured()) {
            return this.totalMonths;
        }

        int yearsPassed = current.getYear() - start.getYear();
        int monthsPassed = current.getMonthValue() - start.getMonthValue();
        return (yearsPassed * 12) + monthsPassed;
    }

    public int getMonthsRemaining() {
        LocalDate current = DateTimeManager.getCurrentDate();
        int yearsLeft = maturityDate.getYear() - current.getYear();
        int monthsLeft = maturityDate.getMonthValue() - current.getMonthValue();

        return Math.max(0, (yearsLeft * 12) + monthsLeft);
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public int getTotalMonths() {
        return this.totalMonths;
    }

    public boolean isMatured() {
        LocalDate current = DateTimeManager.getCurrentDate();
        return current.isAfter(maturityDate) || current.isEqual(maturityDate);
    }

}
