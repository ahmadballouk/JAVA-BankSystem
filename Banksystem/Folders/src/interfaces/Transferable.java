package interfaces;

import model.account.Account;

public interface Transferable {

    void transferOut(double amount, Account transferToAccount);

    void transferIn(double amount, Account transferFromAccount);
}
