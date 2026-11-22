package model;

import java.util.ArrayList;

// Practical 3: INHERITANCE (Expense -> FoodExpense)
public class FoodExpense extends Expense {
    public FoodExpense(String title, double amount, String paidBy, ArrayList<String> participants) {
        super(title, amount, paidBy, participants);
    }
    public String getCategory() { return "Food"; }
    public String getCode() { return "F"; }
}
