package model;

import java.util.ArrayList;

// Practical 3: MULTILEVEL INHERITANCE (Expense -> FoodExpense -> DeliveryExpense)
public class DeliveryExpense extends FoodExpense {
    private double fee;                     // delivery charge + tip

    public DeliveryExpense(String title, double amount, double fee, String paidBy, ArrayList<String> participants) {
        super(title, amount, paidBy, participants);
        this.fee = fee;
    }

    public double getFee() { return fee; }

    public double getTotal() {              // method OVERRIDING: bill + delivery fee
        return amount + fee;
    }

    public String getCategory() { return "Food Delivery"; }
    public String getCode() { return "D"; }
}
