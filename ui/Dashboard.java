package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import service.*;

// Practical 10: GUI - the main window (Swing)
public class Dashboard extends JFrame {
    private static final String FILE = "splitmate.txt";
    private static final Color TEAL = new Color(0, 128, 114);

    private final ExpenseManager manager = new ExpenseManager();

    // form
    private JTextField titleField = new JTextField();
    private JTextField amountField = new JTextField();
    private JComboBox<String> paidByBox = new JComboBox<String>();
    private JComboBox<String> categoryBox =
            new JComboBox<String>(new String[]{"Food", "Travel", "Food Delivery", "Other"});
    private JTextField feeField = new JTextField("0");
    private JComboBox<String> splitBox = new JComboBox<String>(new String[]{"Equal", "Custom"});
    private JTextField sharesField = new JTextField();
    private JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private ArrayList<JCheckBox> checks = new ArrayList<JCheckBox>();

    // tabs
    private JTabbedPane tabs = new JTabbedPane();
    private DefaultTableModel expenseModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Category", "Paid by", "Total (Rs)", "Split between"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private DefaultTableModel balanceModel = new DefaultTableModel(
            new String[]{"Member", "Paid (Rs)", "Share (Rs)", "Net (Rs)", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable expenseTable = new JTable(expenseModel);
    private JTable balanceTable = new JTable(balanceModel);
    private JTextArea settleArea = new JTextArea();

    // bottom
    private JTextArea detailsArea = new JTextArea(4, 40);
    private JLabel reminderLabel = new JLabel(" ");
    private JLabel statusLabel = new JLabel(" ");

    private AutoSaveThread autoSave;
    private ReminderThread reminder;

    public Dashboard(String user) {
        super("SplitMate   |   Logged in as: " + user);
        setSize(820, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        try {
            manager.loadFromFile(FILE);                 // Practical 7: read saved data
        } catch (IOException e) {
            statusLabel.setText("Could not read " + FILE);
        }

        buildScreen();
        rebuildPeople();
        refresh();
        startThreads();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { closeApp(); }
        });
    }

    // ------------------------------------------------------------ screen
    private void buildScreen() {
        JLabel header = new JLabel("  SplitMate  -  Spend together, settle easily.");
        header.setOpaque(true);
        header.setBackground(TEAL);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setBorder(new EmptyBorder(10, 4, 10, 4));

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(buildForm(), BorderLayout.CENTER);

        expenseTable.setRowHeight(20);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });
        balanceTable.setRowHeight(20);
        settleArea.setEditable(false);
        settleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        tabs.addTab("Expenses", new JScrollPane(expenseTable));
        tabs.addTab("Balances", new JScrollPane(balanceTable));
        tabs.addTab("Settle Up", new JScrollPane(settleArea));

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(titled("Expense details (click a row)"));

        reminderLabel.setForeground(new Color(200, 30, 30));
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        detailsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        reminderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        south.add(detailsScroll);
        south.add(reminderLabel);
        south.add(statusLabel);

        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(titled("New expense"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        addRow(form, c, 0, "Title:", titleField, "Amount (Rs):", amountField);
        addRow(form, c, 1, "Paid by:", paidByBox, "Category:", categoryBox);
        addRow(form, c, 2, "Delivery fee:", feeField, "Split type:", splitBox);

        c.gridy = 3; c.gridx = 0; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel("Custom shares (Name=Amount, ...):"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(sharesField, c);

        checkPanel.setBorder(titled("Shared between"));
        c.gridy = 4; c.gridx = 0; c.gridwidth = 4;
        form.add(checkPanel, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.add(button("Add Expense", e -> addExpense()));
        buttons.add(button("Delete Selected", e -> deleteSelected()));
        buttons.add(button("Add Member", e -> addMember()));
        buttons.add(button("Settle Up", e -> { tabs.setSelectedIndex(2); refresh(); }));
        buttons.add(button("Save", e -> save()));
        buttons.add(button("Load", e -> load()));
        buttons.add(button("Clear", e -> clearForm()));
        c.gridy = 5;
        form.add(buttons, c);

        feeField.setEnabled(false);
        sharesField.setEnabled(false);
        categoryBox.addActionListener(e ->
                feeField.setEnabled("Food Delivery".equals(categoryBox.getSelectedItem())));
        splitBox.addActionListener(e -> {
            boolean custom = "Custom".equals(splitBox.getSelectedItem());
            sharesField.setEnabled(custom);
            for (JCheckBox box : checks) box.setEnabled(!custom);
        });
        return form;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row,
                        String l1, JComponent f1, String l2, JComponent f2) {
        c.gridy = row; c.gridwidth = 1;
        c.gridx = 0; c.weightx = 0; p.add(new JLabel(l1), c);
        c.gridx = 1; c.weightx = 1; p.add(f1, c);
        c.gridx = 2; c.weightx = 0; p.add(new JLabel(l2), c);
        c.gridx = 3; c.weightx = 1; p.add(f2, c);
    }

    private JButton button(String text, ActionListener a) {
        JButton b = new JButton(text);
        b.addActionListener(a);
        return b;
    }

    private TitledBorder titled(String text) {
        TitledBorder t = BorderFactory.createTitledBorder(text);
        t.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        return t;
    }

    // ------------------------------------------------------------ actions
    private void addExpense() {
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.contains(",")) {
            warn("Please enter a title (commas are not allowed).");
            return;
        }
        double amount, fee = 0;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException ex) {
            warn("Amount must be a number.");
            return;
        }
        String type = (String) categoryBox.getSelectedItem();
        if (type.equals("Food Delivery")) {
            try {
                fee = feeField.getText().trim().isEmpty() ? 0 : Double.parseDouble(feeField.getText().trim());
            } catch (NumberFormatException ex) {
                warn("Delivery fee must be a number.");
                return;
            }
        }
        String paidBy = (String) paidByBox.getSelectedItem();
        if (paidBy == null) {
            warn("Add at least one member first.");
            return;
        }

        try {
            if ("Custom".equals(splitBox.getSelectedItem())) {
                manager.addExpense(type, title, amount, fee, paidBy, parseShares());   // overloaded: custom
            } else {
                ArrayList<String> people = new ArrayList<String>();
                for (JCheckBox box : checks)
                    if (box.isSelected()) people.add(box.getText());
                manager.addExpense(type, title, amount, fee, paidBy, people);          // overloaded: equal
            }
        } catch (NumberFormatException ex) {
            warn("Custom shares must look like:  Durga=200, Ravi=300");
            return;
        } catch (NegativeAmountException | PersonNotFoundException | InvalidSplitException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot add expense",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        titleField.setText("");
        amountField.setText("");
        sharesField.setText("");
        refresh();
        tabs.setSelectedIndex(0);
    }

    // "Durga=200, Ravi=300"  ->  {Durga=200.0, Ravi=300.0}
    private HashMap<String, Double> parseShares() {
        HashMap<String, Double> shares = new HashMap<String, Double>();
        for (String part : sharesField.getText().split(",")) {
            if (part.trim().isEmpty()) continue;
            String[] kv = part.split("=");
            if (kv.length != 2) throw new NumberFormatException("bad share");
            String name = kv[0].trim();
            for (Member m : manager.getMembers())           // match the name ignoring capital letters
                if (m.getName().equalsIgnoreCase(name)) name = m.getName();
            shares.put(name, Double.parseDouble(kv[1].trim()));
        }
        return shares;
    }

    private void deleteSelected() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            warn("Click an expense in the table first.");
            return;
        }
        int id = (Integer) expenseModel.getValueAt(row, 0);
        manager.deleteExpense(id);
        detailsArea.setText("");
        refresh();
    }

    private void addMember() {
        String name = JOptionPane.showInputDialog(this, "Member name:");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty() || name.matches(".*[,;=].*")) {
            warn("Please enter a name without commas, semicolons or '='.");
            return;
        }
        String email = JOptionPane.showInputDialog(this, "Email (optional):");
        if (email == null) email = "";
        email = email.trim().replace(",", "");
        if (email.isEmpty()) email = "-";
        try {
            manager.addMember(new Member(name, email));
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return;
        }
        rebuildPeople();
        refresh();
    }

    private void save() {
        try {
            manager.saveToFile(FILE);
            statusLabel.setText("Saved to " + FILE);
        } catch (IOException ex) {
            warn("Could not save: " + ex.getMessage());
        }
    }

    private void load() {
        try {
            int n = manager.loadFromFile(FILE);
            rebuildPeople();
            refresh();
            if (n == 0) statusLabel.setText("Nothing to load yet - save first.");
        } catch (IOException ex) {
            warn("Could not load: " + ex.getMessage());
        }
    }

    private void clearForm() {
        titleField.setText("");
        amountField.setText("");
        feeField.setText("0");
        sharesField.setText("");
        categoryBox.setSelectedIndex(0);
        splitBox.setSelectedIndex(0);
        for (JCheckBox box : checks) box.setSelected(true);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "SplitMate", JOptionPane.WARNING_MESSAGE);
    }

    // ------------------------------------------------------------ refresh the screen
    private void rebuildPeople() {
        String keep = (String) paidByBox.getSelectedItem();
        paidByBox.removeAllItems();
        checkPanel.removeAll();
        checks.clear();
        boolean custom = "Custom".equals(splitBox.getSelectedItem());
        for (Member m : manager.getMembers()) {
            paidByBox.addItem(m.getName());
            JCheckBox box = new JCheckBox(m.getName(), true);
            box.setEnabled(!custom);
            checks.add(box);
            checkPanel.add(box);
        }
        if (keep != null) paidByBox.setSelectedItem(keep);
        checkPanel.revalidate();
        checkPanel.repaint();
    }

    private void refresh() {
        // expenses tab
        expenseModel.setRowCount(0);
        for (Expense e : manager.getExpenses()) {
            expenseModel.addRow(new Object[]{e.getId(), e.getTitle(), e.getCategory(), e.getPaidBy(),
                    String.format("%.2f", e.getTotal()), e.getParticipants().toString()});
        }

        // balances tab
        balanceModel.setRowCount(0);
        HashMap<String, Double> bal = manager.balances();
        for (Member m : manager.getMembers()) {
            double net = bal.get(m.getName());
            String status = net > 0.005 ? "gets back" : net < -0.005 ? "has to pay" : "settled";
            balanceModel.addRow(new Object[]{m.getName(),
                    String.format("%.2f", manager.totalPaid(m.getName())),
                    String.format("%.2f", manager.totalShare(m.getName())),
                    String.format("%.2f", net), status});
        }

        // settle up tab
        StringBuilder sb = new StringBuilder();
        sb.append("\n  WHO PAYS WHOM (minimum payments)\n");
        sb.append("  --------------------------------\n");
        ArrayList<String> list = manager.settlements();
        if (list.isEmpty()) sb.append("  Everyone is settled up!\n");
        for (String s : list) sb.append("  ").append(s).append("\n");
        settleArea.setText(sb.toString());
        settleArea.setCaretPosition(0);

        statusLabel.setText("Members: " + manager.getMembers().size()
                + "   |   Expenses: " + manager.getExpenses().size()
                + "   |   Total spent: Rs " + String.format("%.2f", manager.totalSpent())
                + "   |   Expense objects created: " + Expense.getCount());
    }

    private void showDetails() {
        int row = expenseTable.getSelectedRow();
        if (row < 0) return;
        Expense e = manager.findExpense((Integer) expenseModel.getValueAt(row, 0));
        if (e == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Title    : ").append(e.getTitle()).append("\n");
        sb.append("Category : ").append(e.getCategory()).append("\n");
        sb.append("Paid by  : ").append(e.getPaidBy()).append("\n");
        sb.append(String.format("Total    : Rs %.2f", e.getTotal()));
        if (e.getFee() > 0) sb.append(String.format("  (includes delivery fee Rs %.2f)", e.getFee()));
        sb.append("\nSplit    : ").append(e.getCustomShares() != null ? "Custom" : "Equal").append("\n");
        for (String p : e.getParticipants())
            sb.append(String.format("   %-10s Rs %.2f%n", p, e.shareOf(p)));
        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    // ------------------------------------------------------------ threads
    private void startThreads() {
        autoSave = new AutoSaveThread(manager, FILE, msg ->
                SwingUtilities.invokeLater(() -> statusLabel.setText(msg)));
        reminder = new ReminderThread(manager, msg ->
                SwingUtilities.invokeLater(() -> reminderLabel.setText(msg.isEmpty() ? " " : msg)));
        autoSave.start();
        reminder.start();
    }

    private void closeApp() {
        autoSave.stopSaving();
        reminder.stopReminders();
        try {
            manager.saveToFile(FILE);
        } catch (IOException ex) {
            // closing anyway
        }
        dispose();
        System.exit(0);
    }
}
