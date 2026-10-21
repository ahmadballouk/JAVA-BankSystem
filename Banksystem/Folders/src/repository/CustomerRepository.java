package repository;

import java.time.LocalDateTime;
import java.util.List;
import model.customer.Customer;
import service.GlobalData;
import validation.InputValidator;

public class CustomerRepository {

    public Customer createCustomer(String fullName, String phoneNumber, String email) {
        if (!InputValidator.isValidName(fullName)) {
            System.out.println("ERROR: Invalid customer name.");
            return null;
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && !InputValidator.isValidPhoneNumber(phoneNumber)) {
            System.out.println("ERROR: Invalid phone number.");
            return null;
        }
        if (email != null && !email.isBlank() && !InputValidator.isValidEmail(email)) {
            System.out.println("ERROR: Invalid email address.");
            return null;
        }

        Customer customer = new Customer(
                fullName,
                phoneNumber == null || phoneNumber.isBlank() ? null : phoneNumber,
                email == null || email.isBlank() ? null : email,
                LocalDateTime.now());

        System.out.printf("SUCCESS: Customer %s created with ID %s.%n",
                customer.getFullName(), customer.getCustomerIdString());
        return customer;
    }

    public boolean removeCustomer(Customer customer) {
        if (customer == null) {
            System.out.println("ERROR: Customer not found.");
            return false;
        }
        if (customer.isRemoved()) {
            System.out.printf("ERROR: Customer %s has already been removed.%n", customer.getFullName());
            return false;
        }
        if (!customer.canRemoveCustomer()) {
            System.out.printf("ERROR: Cannot remove %s while open accounts remain.%n", customer.getFullName());
            return false;
        }

        customer.setRemoved(true);
        System.out.printf("SUCCESS: Customer %s has been removed.%n", customer.getFullName());
        return true;
    }

    public void displayAllCustomers() {
        List<Customer> customers = GlobalData.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers are registered.");
            return;
        }

        System.out.println("\n===== ALL REGISTERED CUSTOMERS =====");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            System.out.printf("%d. %s | %s | %s%n",
                    i + 1,
                    customer.getCustomerIdString(),
                    customer.getFullName(),
                    customer.isRemoved() ? "REMOVED" : "ACTIVE");
        }
        System.out.println("====================================\n");
    }

    public void displayAllRemovedCustomers() {
        List<Customer> customers = GlobalData.getAllCustomers().stream()
                .filter(Customer::isRemoved)
                .toList();

        if (customers.isEmpty()) {
            System.out.println("No removed customers were found.");
            return;
        }

        System.out.println("\n===== REMOVED CUSTOMERS =====");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            System.out.printf("%d. %s | %s%n",
                    i + 1, customer.getCustomerIdString(), customer.getFullName());
        }
        System.out.println("=============================\n");
    }
}
