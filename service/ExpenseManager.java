package service;

import java.io.*;
import java.util.*;
import model.*;                             // Practical 5: importing our own package

public class ExpenseManager {
    // Practical 9: COLLECTIONS
    private ArrayList<Member> members = new ArrayList<Member>();
    private ArrayList<Expense> expenses = new ArrayList<Expense>();

    // ---------------- members ----------------
    public synchronized void addMember(Member m) {
        if (hasMember(m.getName()))
            throw new IllegalArgumentException(m.getName() + " is already in the group");
        members.add(m);
    }

    public synchronized boolean hasMember(String name) {
        for (Member m : members)
            if (m.getName().equalsIgnoreCase(name)) return true;
        return false;
    }

    public synchronized ArrayList<Member> getMembers() {
        return new ArrayList<Member>(members);
    }

    // ---------------- expenses ----------------
    // The main method: checks everything and throws our own exceptions
    public synchronized void addExpense(Expense e)
            throws NegativeAmountException, PersonNotFoundException, InvalidSplitException {
        if (e.getTotal() <= 0)
            throw new NegativeAmountException("Amount must be greater than zero");
        if (!hasMember(e.getPaidBy()))
            throw new PersonNotFoundException(e.getPaidBy() + " is not in the group");
        if (e.getParticipants().isEmpty())
            throw new InvalidSplitException("Select at least one person to share the expense");
        for (String p : e.getParticipants())
            if (!hasMember(p))
                throw new PersonNotFoundException(p + " is not in the group");
        if (e.getCustomShares() != null) {
            double sum = 0;
            for (double d : e.getCustomShares().values()) sum += d;
            if (Math.abs(sum - e.getTotal()) > 0.01)
                throw new InvalidSplitException("Custom shares add up to Rs " + String.format("%.2f", sum)
                        + " but the expense total is Rs " + String.format("%.2f", e.getTotal()));
        }
        expenses.add(e);
    }

    // Practical 2: METHOD OVERLOADING - same name addExpense, different parameters
    // (1) equal split between the selected people
    public void addExpense(String type, String title, double amount, double fee, String paidBy,
                           ArrayList<String> people)
            throws NegativeAmountException, PersonNotFoundException, InvalidSplitException {
        addExpense(create(type, title, amount, fee, paidBy, people));
    }

    // (2) custom split: name -> amount
    public void addExpense(String type, String title, double amount, double fee, String paidBy,
                           HashMap<String, Double> shares)
            throws NegativeAmountException, PersonNotFoundException, InvalidSplitException {
        Expense e = create(type, title, amount, fee, paidBy, new ArrayList<String>(shares.keySet()));
        e.setCustomShares(shares);
        addExpense(e);
    }

    private Expense create(String type, String title, double amount, double fee, String paidBy,
                           ArrayList<String> people) {
        if (type.equals("Food")) return new FoodExpense(title, amount, paidBy, people);
        if (type.equals("Travel")) return new TravelExpense(title, amount, paidBy, people);
        if (type.equals("Food Delivery")) return new DeliveryExpense(title, amount, fee, paidBy, people);
        return new OtherExpense(title, amount, paidBy, people);
    }

    // Practical 9: ITERATOR used to delete safely
    public synchronized boolean deleteExpense(int id) {
        Iterator<Expense> it = expenses.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == id) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<Expense> getExpenses() {
        return new ArrayList<Expense>(expenses);
    }

    public synchronized Expense findExpense(int id) {
        for (Expense e : expenses)
            if (e.getId() == id) return e;
        return null;
    }

    public synchronized double totalSpent() {
        double t = 0;
        for (Expense e : expenses) t += e.getTotal();
        return t;
    }

    // ---------------- balances ----------------
    public synchronized double totalPaid(String name) {
        double t = 0;
        for (Expense e : expenses)
            if (e.getPaidBy().equals(name)) t += e.getTotal();
        return t;
    }

    public synchronized double totalShare(String name) {
        double t = 0;
        for (Expense e : expenses) t += e.shareOf(name);
        return t;
    }

    // Practical 9: HASHMAP  name -> net balance  (+ gets back, - has to pay)
    public synchronized HashMap<String, Double> balances() {
        HashMap<String, Double> map = new HashMap<String, Double>();
        for (Member m : members)
            map.put(m.getName(), totalPaid(m.getName()) - totalShare(m.getName()));
        return map;
    }

    // Who pays whom: always settle the biggest debtor with the biggest creditor
    public synchronized ArrayList<String> settlements() {
        HashMap<String, Double> bal = balances();
        ArrayList<String> result = new ArrayList<String>();
        while (true) {
            String creditor = null, debtor = null;
            for (String n : bal.keySet()) {
                double v = bal.get(n);
                if (v > 0.005 && (creditor == null || v > bal.get(creditor))) creditor = n;
                if (v < -0.005 && (debtor == null || v < bal.get(debtor))) debtor = n;
            }
            if (creditor == null || debtor == null) break;
            double pay = Math.min(bal.get(creditor), -bal.get(debtor));
            result.add(debtor + " pays " + creditor + " : Rs " + String.format("%.2f", pay));
            bal.put(creditor, bal.get(creditor) - pay);
            bal.put(debtor, bal.get(debtor) + pay);
        }
        return result;
    }

    // ---------------- files ----------------
    // Practical 7: FILE WRITE
    public synchronized void saveToFile(String file) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            for (Member m : members)
                fw.write("M," + m.getName() + "," + m.getEmail() + "\n");
            for (Expense e : expenses)
                fw.write(e.toCsv() + "\n");
        } finally {                         // Practical 6: finally always runs
            if (fw != null) fw.close();
        }
    }

    // Practical 7: FILE READ
    public synchronized int loadFromFile(String file) throws IOException {
        File f = new File(file);
        if (!f.exists()) return 0;
        members.clear();
        expenses.clear();
        Scanner sc = new Scanner(f);
        while (sc.hasNextLine()) {
            String[] p = sc.nextLine().split(",");
            try {
                if (p[0].equals("M")) {
                    members.add(new Member(p[1], p[2]));
                } else if (p[0].equals("E")) {
                    String type = p[1].equals("F") ? "Food" : p[1].equals("T") ? "Travel"
                                : p[1].equals("D") ? "Food Delivery" : "Other";
                    double amount = Double.parseDouble(p[3]);
                    double fee = Double.parseDouble(p[4]);
                    if (p[6].equals("C")) {
                        HashMap<String, Double> shares = new HashMap<String, Double>();
                        for (String s : p[7].split(";")) {
                            String[] kv = s.split("=");
                            shares.put(kv[0], Double.parseDouble(kv[1]));
                        }
                        addExpense(type, p[2], amount, fee, p[5], shares);
                    } else {
                        ArrayList<String> people = new ArrayList<String>(Arrays.asList(p[7].split(";")));
                        addExpense(type, p[2], amount, fee, p[5], people);
                    }
                }
            } catch (Exception ex) {
                // skip a wrong line
            }
        }
        sc.close();
        return expenses.size() + members.size();
    }
}
