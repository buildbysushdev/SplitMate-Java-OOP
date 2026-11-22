package model;

import java.util.ArrayList;

public class TravelExpense extends Expense {
    public TravelExpense(String title, double amount, String paidBy, ArrayList<String> participants) {
        super(title, amount, paidBy, participants);
    }
    public String getCategory() { return "Travel"; }
    public String getCode() { return "T"; }
}
