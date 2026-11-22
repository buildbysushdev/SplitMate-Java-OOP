package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import service.*;

public class Dashboard extends JFrame implements ActionListener, MouseListener {
    static final String FILE = "splitmate.txt";
    ExpenseManager manager = new ExpenseManager();

    // ---- form ----
    JTextField title = new JTextField(), amount = new JTextField(), fee = new JTextField("0"), custom = new JTextField();
    JComboBox<String> payer = new JComboBox<String>();
    JComboBox<String> category = new JComboBox<String>(new String[] {"Food", "Travel", "Food Delivery", "Other"});
    JComboBox<String> split = new JComboBox<String>(new String[] {"Equal", "Custom"});
    JPanel people = new JPanel(new FlowLayout(FlowLayout.LEFT));
    ArrayList<JCheckBox> boxes = new ArrayList<JCheckBox>();

    // ---- tables and areas ----
    DefaultTableModel expModel = new DefaultTableModel(new String[] {"ID", "Title", "Category", "Paid by", "Total (Rs)", "Split between"}, 0);
    JTable expTable = new JTable(expModel);
    DefaultTableModel balModel = new DefaultTableModel(new String[] {"Member", "Paid (Rs)", "Share (Rs)", "Net (Rs)", "Status"}, 0);
    JTable balTable = new JTable(balModel);
    JTextArea settleArea = new JTextArea();
    JTextArea details = new JTextArea(3, 20);
    JTabbedPane tabs = new JTabbedPane();
    JLabel status = new JLabel("Ready");
    JLabel reminder = new JLabel("Reminder: ...");

    public Dashboard(String username) {
        super("SplitMate   |   Logged in as: " + username);
        setLayout(new BorderLayout());

        JLabel banner = new JLabel("  SplitMate  -  Spend together, settle easily.", JLabel.LEFT);
        banner.setOpaque(true);
        banner.setBackground(new Color(0, 121, 107));
        banner.setForeground(Color.WHITE);
        banner.setFont(new Font("SansSerif", Font.BOLD, 18));
        banner.setPreferredSize(new Dimension(100, 36));

        // ---- expense form ----
        JPanel form = new JPanel(new GridLayout(3, 4, 6, 6));
        form.add(new JLabel("Title:"));        form.add(title);
        form.add(new JLabel("Amount (Rs):"));  form.add(amount);
        form.add(new JLabel("Paid by:"));      form.add(payer);
        form.add(new JLabel("Category:"));     form.add(category);
        form.add(new JLabel("Delivery fee:")); form.add(fee);
        form.add(new JLabel("Split type:"));   form.add(split);
        JPanel customRow = new JPanel(new BorderLayout(6, 0));
        customRow.add(new JLabel("Custom shares (Name=Amount, ...):"), BorderLayout.WEST);
        customRow.add(custom, BorderLayout.CENTER);
        people.setBorder(BorderFactory.createTitledBorder("Shared between"));

        title.setName("title"); amount.setName("amount"); fee.setName("fee"); custom.setName("custom");
        payer.setName("payer"); category.setName("category"); split.setName("split");
        expTable.setName("expTable"); balTable.setName("balTable"); settleArea.setName("settleArea");
        details.setName("details"); status.setName("status"); reminder.setName("reminder"); tabs.setName("tabs");
        fee.setEnabled(false);
        custom.setEnabled(false);
        category.setActionCommand("category");  category.addActionListener(this);
        split.setActionCommand("split");        split.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout());
        String[][] btn = {{"Add Expense", "add"}, {"Delete Selected", "delete"}, {"Add Member", "member"},
                          {"Settle Up", "settle"}, {"Save", "save"}, {"Load", "load"}, {"Clear", "clear"}};
        for (String[] b : btn) {
            JButton jb = new JButton(b[0]);
            jb.setName(b[1]);
            jb.setActionCommand(b[1]);
            jb.addActionListener(this);          // Practical 11: ActionListener
            buttons.add(jb);
        }

        JPanel input = new JPanel(new BorderLayout(4, 4));
        input.setBorder(BorderFactory.createTitledBorder("New expense"));
        JPanel formAll = new JPanel(new BorderLayout(4, 4));
        formAll.add(form, BorderLayout.NORTH);
        formAll.add(customRow, BorderLayout.CENTER);
        formAll.add(people, BorderLayout.SOUTH);
        input.add(formAll, BorderLayout.CENTER);
        input.add(buttons, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(banner, BorderLayout.NORTH);
        top.add(input, BorderLayout.CENTER);

        // ---- tabs ----
        expTable.addMouseListener(this);         // Practical 11: MouseListener
        settleArea.setEditable(false);
        settleArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        details.setEditable(false);
        details.setBorder(BorderFactory.createTitledBorder("Expense details (click a row)"));
        tabs.addTab("Expenses", new JScrollPane(expTable));
        tabs.addTab("Balances", new JScrollPane(balTable));
        tabs.addTab("Settle Up", new JScrollPane(settleArea));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JScrollPane(details), BorderLayout.NORTH);
        JPanel bars = new JPanel(new GridLayout(2, 1));
        reminder.setForeground(new Color(183, 28, 28));
        bars.add(reminder);
        bars.add(status);
        bottom.add(bars, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // ---- load old data or sample data ----
        try {
            if (manager.loadFromFile(FILE) == 0) addSampleData();
        } catch (IOException e) {
            status.setText("Could not read file");
        }
        rebuildMembers();
        refresh();

        new AutoSaveThread(manager, FILE, status, 10).start();     // Practical 8: thread 1
        new ReminderThread(manager, reminder, 4).start();          // Practical 8: thread 2

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try { manager.saveToFile(FILE); } catch (IOException ex) { }
            }
        });
        setSize(820, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    void addSampleData() {
        try {
            manager.addMember(new Member("Sreeram", "sreeram@gmail.com"));
            manager.addMember(new Member("Durga", "durga@gmail.com"));
            manager.addMember(new Member("Aditya", "aditya@gmail.com"));
            manager.addMember(new Member("Surya", "surya@gmail.com"));
            manager.addMember(new Member("Ravi", "ravi@gmail.com"));
            manager.addExpense("Food", "Biryani dinner", 1200, 0, "Aditya",
                    new ArrayList<String>(Arrays.asList("Sreeram", "Durga", "Aditya", "Surya")));
            manager.addExpense("Travel", "Goa trip cab", 2500, 0, "Surya",
                    new ArrayList<String>(Arrays.asList("Sreeram", "Durga", "Aditya", "Surya", "Ravi")));
            manager.addExpense("Food Delivery", "Pizza order", 800, 50, "Sreeram",
                    new ArrayList<String>(Arrays.asList("Sreeram", "Ravi", "Durga")));
            HashMap<String, Double> shares = new HashMap<String, Double>();
            shares.put("Ravi", 200.0);
            shares.put("Sreeram", 400.0);
            shares.put("Aditya", 300.0);
            manager.addExpense("Other", "Movie tickets", 900, 0, "Ravi", shares);
        } catch (Exception e) {
            status.setText("Sample data error: " + e.getMessage());
        }
    }

    // member names -> combo box and check boxes
    void rebuildMembers() {
        payer.removeAllItems();
        people.removeAll();
        boxes.clear();
        for (Member m : manager.getMembers()) {
            payer.addItem(m.getName());
            JCheckBox cb = new JCheckBox(m.getName(), true);
            cb.setName("cb_" + m.getName());
            boxes.add(cb);
            people.add(cb);
        }
        people.revalidate();
        people.repaint();
    }

    void refresh() {
        expModel.setRowCount(0);
        for (Expense e : manager.getExpenses())
            expModel.addRow(new Object[] {e.getId(), e.getTitle(), e.getCategory(), e.getPaidBy(),
                    String.format("%.2f", e.getTotal()), e.getParticipants().toString()});

        balModel.setRowCount(0);
        HashMap<String, Double> bal = manager.balances();
        for (Member m : manager.getMembers()) {
            double net = bal.get(m.getName());
            String st = net > 0.005 ? "gets back" : net < -0.005 ? "has to pay" : "settled";
            balModel.addRow(new Object[] {m.getName(), String.format("%.2f", manager.totalPaid(m.getName())),
                    String.format("%.2f", manager.totalShare(m.getName())), String.format("%.2f", net), st});
        }

        ArrayList<String> s = manager.settlements();
        StringBuilder sb = new StringBuilder("  WHO PAYS WHOM (minimum payments)\n  ---------------------------------\n");
        if (s.isEmpty()) sb.append("  Everyone is settled up!\n");
        for (String line : s) sb.append("  ").append(line).append("\n");
        settleArea.setText(sb.toString());

        status.setText("Members: " + manager.getMembers().size() + "   |   Expenses: " + manager.getExpenses().size()
                + "   |   Total spent: Rs " + String.format("%.2f", manager.totalSpent())
                + "   |   Expense objects created: " + Expense.getCount());
    }

    void clearForm() {
        title.setText(""); amount.setText(""); fee.setText("0"); custom.setText("");
        category.setSelectedIndex(0); split.setSelectedIndex(0);
        for (JCheckBox cb : boxes) cb.setSelected(true);
    }

    // Practical 11: every button / combo box action comes here
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        try {
            if (cmd.equals("category")) {
                fee.setEnabled(category.getSelectedItem().equals("Food Delivery"));
            } else if (cmd.equals("split")) {
                custom.setEnabled(split.getSelectedItem().equals("Custom"));
            } else if (cmd.equals("add")) {
                String t = title.getText().trim().replace(",", " ");
                if (t.isEmpty()) throw new IllegalArgumentException("Please enter a title");
                double amt = Double.parseDouble(amount.getText().trim());     // may throw NumberFormatException
                double fe = category.getSelectedItem().equals("Food Delivery") ? Double.parseDouble(fee.getText().trim()) : 0;
                String paidBy = (String) payer.getSelectedItem();
                String type = (String) category.getSelectedItem();
                if (split.getSelectedItem().equals("Equal")) {
                    ArrayList<String> sel = new ArrayList<String>();
                    for (JCheckBox cb : boxes) if (cb.isSelected()) sel.add(cb.getText());
                    manager.addExpense(type, t, amt, fe, paidBy, sel);       // overloaded method 1
                } else {
                    HashMap<String, Double> shares = new HashMap<String, Double>();
                    for (String part : custom.getText().split(",")) {
                        String[] kv = part.split("=");
                        shares.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                    manager.addExpense(type, t, amt, fe, paidBy, shares);    // overloaded method 2
                }
                clearForm();
                refresh();
                tabs.setSelectedIndex(0);
                status.setText("Expense added.   " + status.getText());
            } else if (cmd.equals("delete")) {
                int row = expTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select an expense in the table first");
                } else {
                    manager.deleteExpense((Integer) expModel.getValueAt(row, 0));
                    details.setText("");
                    refresh();
                }
            } else if (cmd.equals("member")) {
                String n = JOptionPane.showInputDialog(this, "Member name:");
                if (n != null && !n.trim().isEmpty()) {
                    String em = JOptionPane.showInputDialog(this, "Email:");
                    manager.addMember(new Member(n.trim().replace(",", " "), em == null ? "NA" : em.trim().replace(",", " ")));
                    rebuildMembers();
                    refresh();
                }
            } else if (cmd.equals("settle")) {
                refresh();
                tabs.setSelectedIndex(2);
            } else if (cmd.equals("clear")) {
                clearForm();
            } else if (cmd.equals("save")) {
                manager.saveToFile(FILE);
                status.setText("Saved to " + FILE);
            } else if (cmd.equals("load")) {
                manager.loadFromFile(FILE);
                rebuildMembers();
                refresh();
                status.setText("Loaded from " + FILE + "   |   " + status.getText());
            }
        } catch (NumberFormatException ex) {                                   // Practical 6: exceptions
            JOptionPane.showMessageDialog(this, "Please enter valid numbers (amount, fee, custom shares)", "Input error", JOptionPane.ERROR_MESSAGE);
        } catch (ArrayIndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(this, "Custom shares must look like  Durga=200, Ravi=300", "Input error", JOptionPane.ERROR_MESSAGE);
        } catch (NegativeAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid amount", JOptionPane.ERROR_MESSAGE);
        } catch (PersonNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Person not found", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidSplitException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid split", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File error: " + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Practical 11: mouse events on the expense table
    public void mouseClicked(MouseEvent e) {
        int row = expTable.getSelectedRow();
        if (row >= 0) {
            Expense x = manager.findExpense((Integer) expModel.getValueAt(row, 0));
            if (x != null) {
                Settleable s = x;                                               // interface reference
                StringBuilder sb = new StringBuilder(x.getTitle() + " (" + x.getCategory() + ")  Rs "
                        + String.format("%.2f", x.getTotal()) + "  paid by " + x.getPaidBy() + "\n");
                for (String p : x.getParticipants())
                    sb.append("   ").append(p).append(" owes Rs ").append(String.format("%.2f", s.shareOf(p))).append("\n");
                details.setText(sb.toString());
            }
        }
    }
    public void mouseEntered(MouseEvent e) { status.setText("Click an expense to see who owes what"); }
    public void mouseExited(MouseEvent e) { refresh(); }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
}
