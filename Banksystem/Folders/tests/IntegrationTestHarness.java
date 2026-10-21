import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import menu.*;
import model.customer.Customer;
import model.account.*;
import model.transaction.Transaction;
import enums.AccountType;
import enums.TransactionType;
import service.GlobalData;

public class IntegrationTestHarness {
 static int passed=0,failed=0;
 static void test(String n, boolean ok){if(ok){passed++;System.out.println("PASS: "+n);}else{failed++;System.out.println("FAIL: "+n);}}
 static String output(Runnable r){PrintStream old=System.out;ByteArrayOutputStream b=new ByteArrayOutputStream();try{System.setOut(new PrintStream(b));r.run();}finally{System.setOut(old);}return b.toString();}
 public static void main(String[] args){
  LocalDateTime now=LocalDateTime.now();
  Customer a=new Customer("Alice Example","123456789","alice@example.com",now);
  Customer b=new Customer("Bob Example","123456780","bob@example.com",now);
  CustomerSelectionHelper cExact=new CustomerSelectionHelper(new Scanner("2\n"+a.getCustomerIdString()+"\n1\n"));
  test("customer menu exact ID", cExact.selectCustomer()==a);
  CustomerSelectionHelper cFuzzy=new CustomerSelectionHelper(new Scanner("2\nC00X\n1\n"));
  test("customer menu fuzzy ID suggestion", cFuzzy.selectCustomer()!=null);
  CustomerSelectionHelper cPunctuation=new CustomerSelectionHelper(new Scanner("2\n!!!\n"));
  test("customer menu punctuation query safely returns no match", cPunctuation.selectCustomer()==null);
  CustomerSelectionHelper cName=new CustomerSelectionHelper(new Scanner("3\nAlic Exampel\n1\n"));
  test("customer menu fuzzy name typo", cName.selectCustomer()==a);
  CustomerSelectionHelper cEmail=new CustomerSelectionHelper(new Scanner("4\nalice@example.co\n1\n"));
  test("customer menu email partial", cEmail.selectCustomer()==a);
  CustomerSelectionHelper cPhone=new CustomerSelectionHelper(new Scanner("5\n12345678\n1\n"));
  test("customer menu phone partial", cPhone.selectCustomer()==a);
  CustomerSelectionHelper cBadOption=new CustomerSelectionHelper(new Scanner("99\n"));
  test("customer menu invalid option safely returns null", cBadOption.selectCustomer()==null);
  b.setRemoved(true);
  CustomerSelectionHelper cRemoved=new CustomerSelectionHelper(new Scanner("2\n"+b.getCustomerIdString()+"\n"));
  test("removed customer excluded from selection", cRemoved.selectCustomer()==null);
  SavingsAccount account=new SavingsAccount(a,AccountType.SAVINGS,now,100.0); a.addAccount(account);
  AccountSelectionHelper accountExact=new AccountSelectionHelper(new Scanner("4\n"+account.getAccountNumber()+"\n1\n"));
  test("account menu exact ID", accountExact.selectAccount()==account);
  AccountSelectionHelper accountFuzzy=new AccountSelectionHelper(new Scanner("4\n"+account.getAccountNumber().substring(0,account.getAccountNumber().length()-1)+"X\n1\n"));
  test("account menu fuzzy ID suggestion", accountFuzzy.selectAccount()==account);
  AccountSelectionHelper accountMissing=new AccountSelectionHelper(new Scanner("4\nXYZ999\n"));
  test("account menu unknown ID safely returns null", accountMissing.selectAccount()==null);
  SavingsAccount frozenAccount=new SavingsAccount(a,AccountType.SAVINGS,now,0); a.addAccount(frozenAccount); frozenAccount.setAccountStatus(enums.AccountStatus.FROZEN);
  AccountSelectionHelper accountFrozen=new AccountSelectionHelper(new Scanner("4\n"+frozenAccount.getAccountNumber()+"\n1\n"));
  test("account menu exact ID finds FROZEN account", accountFrozen.selectAccount()==frozenAccount);
  SavingsAccount closedAccount=new SavingsAccount(a,AccountType.SAVINGS,now,0); a.addAccount(closedAccount); closedAccount.setAccountStatus(enums.AccountStatus.CLOSED);
  AccountSelectionHelper accountClosed=new AccountSelectionHelper(new Scanner("4\n"+closedAccount.getAccountNumber()+"\n1\n"));
  test("account menu exact ID finds CLOSED account", accountClosed.selectAccount()==closedAccount);
  Transaction tx=new Transaction(account,TransactionType.Deposit,10.0); tx.markAsCompleted(account.getBalance());
  TransactionSelectionHelper txExact=new TransactionSelectionHelper(new Scanner("3\n"+tx.getTransactionId()+"\n1\n"));
  test("transaction menu exact ID", txExact.selectTransaction()==tx);
  TransactionSelectionHelper txFuzzy=new TransactionSelectionHelper(new Scanner("3\nTXN00000X\n1\n"));
  test("transaction menu fuzzy ID suggestion", txFuzzy.selectTransaction()==tx);
  TransactionSelectionHelper txPunctuation=new TransactionSelectionHelper(new Scanner("3\n!!!\n"));
  test("transaction menu punctuation query safely returns no match", txPunctuation.selectTransaction()==null);
  Transaction failedTx=new Transaction(account,TransactionType.Withdrawal,2.0); failedTx.markAsFailed(account.getBalance());
  TransactionSelectionHelper txFailedStatus=new TransactionSelectionHelper(new Scanner("3\n"+failedTx.getTransactionId()+"\n1\n"));
  test("transaction ID search finds FAILED transaction", txFailedStatus.selectTransaction()==failedTx);
  Transaction pausedTx=new Transaction(account,TransactionType.transfer_out,3.0); pausedTx.markAsPaused(account.getBalance(),account); pausedTx.setDestinationAccount(closedAccount);
  TransactionSelectionHelper txPausedStatus=new TransactionSelectionHelper(new Scanner("3\n"+pausedTx.getTransactionId()+"\n1\n"));
  test("transaction ID search finds PAUSED transaction", txPausedStatus.selectTransaction()==pausedTx);
  System.out.println("RESULT: passed="+passed+" failed="+failed); if(failed>0)System.exit(1);
 }
}
