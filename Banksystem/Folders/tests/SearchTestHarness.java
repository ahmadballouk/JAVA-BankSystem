import java.time.LocalDateTime;
import java.util.*;
import menu.*;
import model.customer.Customer;
import model.account.*;
import model.transaction.Transaction;
import enums.AccountType;
import enums.TransactionType;
import service.GlobalData;

public class SearchTestHarness {
 static int passed=0, failed=0;
 static void test(String name, boolean ok) { if(ok){passed++;System.out.println("PASS: "+name);}else{failed++;System.out.println("FAIL: "+name);} }
 public static void main(String[] args) {
  List<String> ids=List.of("C001","C002","C003","C004","C005");
  test("binary first", "C001".equals(IdBinarySearch.find(ids,"C001",x->x)));
  test("binary middle", "C003".equals(IdBinarySearch.find(ids,"C003",x->x)));
  test("binary last", "C005".equals(IdBinarySearch.find(ids,"C005",x->x)));
  test("case insensitive", "C003".equals(IdBinarySearch.find(ids,"c003",x->x)));
  test("trims entered ID", "C003".equals(IdBinarySearch.find(ids," C003 ",x->x)));
  test("missing returns null", IdBinarySearch.find(ids,"C999",x->x)==null);
  test("null list", IdBinarySearch.find((List<String>) null,"C001",x->x)==null);
  test("empty list", IdBinarySearch.find(List.<String>of(),"C001",x->x)==null);
  test("blank id", IdBinarySearch.find(ids,"   ",x->x)==null);
  test("null getter", IdBinarySearch.find(ids,"C001",null)==null);
  test("fuzzy one typo", SearchMatcher.matches("ACC01023","ACC01032"));
  test("fuzzy partial", SearchMatcher.matches("ACC01023","ACC0102"));
  test("fuzzy case", SearchMatcher.matches("Alice Example","alice example"));
  test("fuzzy substring", SearchMatcher.matches("alice@example.com","alice"));
  test("fuzzy unrelated", !SearchMatcher.matches("ACC01023","XYZ999"));
  test("punctuation-only query should not match all (known-risk check)", !SearchMatcher.matches("ACC01023","!!!"));
  test("non-Latin query should not match all (known-risk check)", !SearchMatcher.matches("Alice Example","你好"));
  test("Unicode name exact match remains searchable", SearchMatcher.matches("أحمد الحسين","أحمد الحسين"));
  LocalDateTime now=LocalDateTime.now();
  Customer c1=new Customer("Alice Example","123456789","alice@example.com",now);
  Customer c2=new Customer("Bob Example","123456780","bob@example.com",now);
  test("customer list sorted by ID", GlobalData.getAllCustomers().get(0).getCustomerIdString().compareToIgnoreCase(GlobalData.getAllCustomers().get(1).getCustomerIdString())<=0);
  Customer exact=IdBinarySearch.find(GlobalData.getAllCustomers(),c2.getCustomerIdString(),Customer::getCustomerIdString);
  test("actual customer exact lookup", exact==c2);
  Customer typo=GlobalData.getAllCustomers().stream().filter(c->SearchMatcher.matches(c.getCustomerIdString(), "C00X")).findFirst().orElse(null);
  test("actual customer fuzzy ID candidate exists for typo", typo!=null);
  c2.setRemoved(true);
  test("removed flag changes status", c2.isRemoved());
  test("account list empty safely searched", IdBinarySearch.find(GlobalData.getAllAccounts(),"ACC0",Account::getAccountNumber)==null);
  test("transaction list empty safely searched", IdBinarySearch.find(GlobalData.getAllTransactions(),"TXN000001",Transaction::getTransactionId)==null);
  System.out.println("RESULT: passed="+passed+" failed="+failed);
  if(failed>0) System.exit(1);
 }
}
