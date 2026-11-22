package model;

import java.util.ArrayList;

public class OtherExpense extends Expense {
    public OtherExpense(String title, double amount, String paidBy, ArrayList<String> participants) {
        super(title, amount, paidBy, participants);
    }
    public String getCategory() { return "Other"; }
    public String getCode() { return "O"; }
}
