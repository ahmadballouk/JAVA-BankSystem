import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Field;
import enums.*;
import model.customer.Customer;
import model.account.*;
import model.transaction.Transaction;
import service.GlobalData;

public class FixedDepositScenarioHarness {
 static int passed=0,failed=0;
 static void test(String n,boolean ok){if(ok){passed++;System.out.println("PASS: "+n);}else{failed++;System.out.println("FAIL: "+n);}}
 static void mature(FixedDepositAccount fd)throws Exception{Field f=fd.getTerm().getClass().getDeclaredField("maturityDate");f.setAccessible(true);f.set(fd.getTerm(),LocalDate.now().minusDays(1));}
 static Customer customer(String name,String email,LocalDateTime now){return new Customer(name,"123456789",email,now);}
 static SavingsAccount linked(Customer owner,LocalDateTime now,double balance){SavingsAccount a=new SavingsAccount(owner,AccountType.SAVINGS,now,balance);owner.addAccount(a);return a;}
 static FixedDepositAccount fd(Customer owner,Account linked,LocalDateTime now,FixedDepositAccountType type){FixedDepositAccount fd=new FixedDepositAccount(owner,AccountType.FIXED_DEPOSIT,1000,6,linked,type,now);owner.addAccount(fd);return fd;}
 public static void main(String[] args)throws Exception{
  LocalDateTime now=LocalDateTime.now();
  Customer owner1=customer("Renew Owner","renew@example.com",now); FixedDepositAccount both=fd(owner1,null,now,FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_AND_INTEREST); mature(both); both.processEndOfMonth();
  test("auto-renew principal and interest retains principal plus interest",Math.abs(both.getBalance()-1018.75)<0.01);
  test("auto-renew principal and interest stays active",both.getAccountStatus()==AccountStatus.ACTIVE);
  test("auto-renew principal and interest starts a new term",both.getTerm().getMaturityDate().isAfter(LocalDate.now()));
  Customer owner2=customer("Principal Owner","principal@example.com",now); SavingsAccount activeLinked=linked(owner2,now,100); FixedDepositAccount principalOnly=fd(owner2,activeLinked,now,FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_ONLY); mature(principalOnly); principalOnly.processEndOfMonth();
  test("principal-only renewal keeps principal balance",Math.abs(principalOnly.getBalance()-1000)<0.01);
  test("principal-only renewal pays interest to linked account",Math.abs(activeLinked.getBalance()-118.75)<0.01);
  test("principal-only renewal stays active",principalOnly.getAccountStatus()==AccountStatus.ACTIVE);
  Customer owner3=customer("Paused Owner","paused@example.com",now); SavingsAccount frozenLinked=linked(owner3,now,100); frozenLinked.setAccountStatus(AccountStatus.FROZEN); FixedDepositAccount pausedPrincipalOnly=fd(owner3,frozenLinked,now,FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_ONLY); mature(pausedPrincipalOnly); pausedPrincipalOnly.processEndOfMonth();
  Transaction paused=GlobalData.getAllTransactions().stream().filter(t->t.getDestinationAccount()==frozenLinked&&t.getTransactionStatus()==TransactionStatus.PAUSED).findFirst().orElse(null);
  test("principal-only renewal pauses interest payout to frozen linked account",paused!=null);
  test("principal-only renewal does not reduce renewed principal",Math.abs(pausedPrincipalOnly.getBalance()-1000)<0.01);
  frozenLinked.activateAccount();
  test("activating linked account completes paused interest payout",Math.abs(frozenLinked.getBalance()-118.75)<0.01&&paused!=null&&paused.getTransactionStatus()==TransactionStatus.COMPLETED);
  Customer owner4=customer("Close Owner","close@example.com",now); SavingsAccount closeLinked=linked(owner4,now,100); closeLinked.setAccountStatus(AccountStatus.FROZEN); FixedDepositAccount close=fd(owner4,closeLinked,now,FixedDepositAccountType.CLOSE_AND_PAYOUT); mature(close); close.processEndOfMonth();
  Transaction closePaused=GlobalData.getAllTransactions().stream().filter(t->t.getDestinationAccount()==closeLinked&&t.getTransactionStatus()==TransactionStatus.PAUSED).findFirst().orElse(null);
  test("close-and-payout closes FD while payout is paused",close.getAccountStatus()==AccountStatus.CLOSED&&Math.abs(close.getBalance())<0.01);
  test("closed FD moves to closed accounts when payout is paused",owner4.getAllAccounts().contains(close)&&!owner4.getAccounts().contains(close));
  closeLinked.activateAccount();
  test("activating linked account completes full paused payout",Math.abs(closeLinked.getBalance()-1118.75)<0.01&&closePaused!=null&&closePaused.getTransactionStatus()==TransactionStatus.COMPLETED);
  test("paused close payout does not reopen or rebalance FD",close.getAccountStatus()==AccountStatus.CLOSED&&Math.abs(close.getBalance())<0.01);
  Customer owner5=customer("Iteration Owner","iteration@example.com",now);
  SavingsAccount first=linked(owner5,now,100);
  FixedDepositAccount closeWithoutDestination=fd(owner5,null,now,FixedDepositAccountType.CLOSE_AND_PAYOUT);
  SavingsAccount afterFd=linked(owner5,now,100);
  mature(closeWithoutDestination);
  boolean endOfMonthCompleted=true;
  try { owner5.processEndOfMonth(); } catch (java.util.ConcurrentModificationException ex) { endOfMonthCompleted=false; }
  test("customer end-of-month safely handles FD moving to closed accounts during iteration",endOfMonthCompleted);
  test("customer end-of-month still processes accounts after closing FD",afterFd.getBalance()>100.0);
  test("matured close-and-payout FD is moved to closed accounts",closeWithoutDestination.getAccountStatus()==AccountStatus.CLOSED && owner5.getAllAccounts().contains(closeWithoutDestination) && !owner5.getAccounts().contains(closeWithoutDestination));
  System.out.println("RESULT: passed="+passed+" failed="+failed);if(failed>0)System.exit(1);
 }
}
