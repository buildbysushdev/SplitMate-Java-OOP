package model;

import java.util.*;

// Practical 4: ABSTRACT CLASS implementing an INTERFACE
public abstract class Expense implements Settleable {
    private static int count = 0;           // Practical 2: STATIC variable (one copy for all)

    protected int id;
    protected String title;
    protected double amount;
    protected String paidBy;
    protected ArrayList<String> participants = new ArrayList<String>();
    protected HashMap<String, Double> customShares;     // null means equal split

    public Expense() {                      // Practical 1: default constructor
        id = ++count;
        title = "Untitled";
        amount = 0;
        paidBy = "";
    }

    public Expense(String title, double amount, String paidBy, ArrayList<String> participants) {
        id = ++count;                       // parameterized constructor
        this.title = title;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = new ArrayList<String>(participants);
    }

    public Expense(Expense e) {             // copy constructor
        this(e.title, e.amount, e.paidBy, e.participants);
        this.customShares = e.customShares;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPaidBy() { return paidBy; }
    public ArrayList<String> getParticipants() { return participants; }
    public HashMap<String, Double> getCustomShares() { return customShares; }

    public void setCustomShares(HashMap<String, Double> shares) {
        customShares = shares;
        participants = new ArrayList<String>(shares.keySet());
    }

    public abstract String getCategory();   // every child class must write this

    public double getTotal() {              // child classes may override
        return amount;
    }

    public double shareOf(String person) {
        if (customShares != null) {
            Double d = customShares.get(person);
            return d == null ? 0 : d;
        }
        if (!participants.contains(person)) return 0;
        return getTotal() / participants.size();
    }

    public double getFee() { return 0; }    // only delivery expenses have a fee

    public abstract String getCode();       // F, D, T or O (used in the file)

    public String toCsv() {                 // one line of the file
        StringBuilder p = new StringBuilder();
        if (customShares != null) {
            for (String n : customShares.keySet())
                p.append(n).append("=").append(customShares.get(n)).append(";");
        } else {
            for (String n : participants) p.append(n).append(";");
        }
        return "E," + getCode() + "," + title + "," + amount + "," + getFee() + "," + paidBy + ","
                + (customShares != null ? "C" : "E") + "," + p;
    }

    public static int getCount() {          // Practical 2: static method
        return count;
    }
}
