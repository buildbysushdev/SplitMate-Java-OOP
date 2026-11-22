import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

// ==========================================
// 1. CUSTOM EXCEPTION
// ==========================================

class InvalidAmountException extends Exception {
    InvalidAmountException(String message) {
        super(message);
    }
}

// ==========================================
// 2. INTERFACE
// ==========================================

interface Reportable {
    String generateReport();
}

// ==========================================
// 3. ABSTRACT CLASS
// ==========================================

abstract class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    int id;
    String date;
    String category;
    double amount;
    String description;

    Transaction(int id, String date, String category,
                double amount, String description) {

        this.id = id;
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    abstract String getType();
}

// ==========================================
// 4. INCOME CLASS
// ==========================================

class Income extends Transaction {

    private static final long serialVersionUID = 1L;

    Income(int id, String date, String category,
           double amount, String description) {

        super(id, date, category, amount, description);
    }

    @Override
    String getType() {
        return "Income";
    }
}

// ==========================================
// 5. EXPENSE CLASS
// ==========================================

class Expense extends Transaction {

    private static final long serialVersionUID = 1L;

    Expense(int id, String date, String category,
            double amount, String description) {

        super(id, date, category, amount, description);
    }

    @Override
    String getType() {
        return "Expense";
    }
}

// ==========================================
// 6. EXPENSE MANAGER CLASS
// ==========================================

class ExpenseManager implements Reportable {

    ArrayList<Transaction> transactions = new ArrayList<>();

    HashMap<String, Double> categoryExpenses = new HashMap<>();

    static int counter = 1;

    // Method overloading - Method 1
    synchronized void addTransaction(Transaction t) {

        transactions.add(t);
        counter = Math.max(counter, t.id + 1);
    }

    // Method overloading - Method 2
    synchronized void addTransaction(
            String type, String date, String category,
            double amount, String description)
            throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException(
                "Amount must be greater than zero!"
            );
        }

        Transaction t;

        if (type.equals("Income")) {

            t = new Income(counter++, date, category,
                           amount, description);

        } else {

            t = new Expense(counter++, date, category,
                            amount, description);
        }

        addTransaction(t);
    }

    // Calculate total income
    synchronized double getIncome() {

        double total = 0;

        for (Transaction t : transactions) {

            if (t.getType().equals("Income")) {
                total += t.amount;
            }
        }

        return total;
    }

    // Calculate total expenses
    synchronized double getExpense() {

        double total = 0;

        for (Transaction t : transactions) {

            if (t.getType().equals("Expense")) {
                total += t.amount;
            }
        }

        return total;
    }

    // Calculate remaining balance
    synchronized double getBalance() {

        return getIncome() - getExpense();
    }

    // Search transactions
    synchronized ArrayList<Transaction> search(String keyword) {

        ArrayList<Transaction> result = new ArrayList<>();

        Iterator<Transaction> iterator =
            transactions.iterator();

        while (iterator.hasNext()) {

            Transaction t = iterator.next();

            if (String.valueOf(t.id).contains(keyword)
                    || t.category.toLowerCase().contains(
                        keyword.toLowerCase())
                    || t.description.toLowerCase().contains(
                        keyword.toLowerCase())
                    || t.date.contains(keyword)) {

                result.add(t);
            }
        }

        return result;
    }

    // Delete transaction
    synchronized void deleteTransaction(int id) {

        Iterator<Transaction> iterator =
            transactions.iterator();

        while (iterator.hasNext()) {

            Transaction t = iterator.next();

            if (t.id == id) {
                iterator.remove();
                break;
            }
        }
    }

    // Update transaction
    synchronized void updateTransaction(
            int id, String type, String date,
            String category, double amount,
            String description)
            throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException(
                "Amount must be greater than zero!"
            );
        }

        for (int i = 0; i < transactions.size(); i++) {

            Transaction old = transactions.get(i);

            if (old.id == id) {

                Transaction updated;

                if (type.equals("Income")) {

                    updated = new Income(
                        id, date, category, amount, description
                    );

                } else {

                    updated = new Expense(
                        id, date, category, amount, description
                    );
                }

                transactions.set(i, updated);
                return;
            }
        }
    }

    // File handling - Save data
    synchronized void saveData() {

        try (ObjectOutputStream out =
                new ObjectOutputStream(
                    new FileOutputStream("transactions.dat"))) {

            out.writeObject(transactions);

        } catch (IOException e) {

            System.out.println(
                "Error saving data: " + e.getMessage()
            );
        }
    }

    // File handling - Load data
    @SuppressWarnings("unchecked")
    synchronized void loadData() {

        File file = new File("transactions.dat");

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in =
                new ObjectInputStream(
                    new FileInputStream(file))) {

            transactions =
                (ArrayList<Transaction>) in.readObject();

            for (Transaction t : transactions) {
                counter = Math.max(counter, t.id + 1);
            }

        } catch (IOException | ClassNotFoundException e) {

            System.out.println(
                "Error loading data: " + e.getMessage()
            );
        }
    }

    // Category-wise report
    @Override
    synchronized public String generateReport() {

        categoryExpenses.clear();

        for (Transaction t : transactions) {

            if (t.getType().equals("Expense")) {

                double oldValue =
                    categoryExpenses.getOrDefault(
                        t.category, 0.0
                    );

                categoryExpenses.put(
                    t.category, oldValue + t.amount
                );
            }
        }

        StringBuilder report = new StringBuilder();

        report.append("PERSONAL EXPENSE REPORT\n");
        report.append("=======================\n\n");

        for (String category : categoryExpenses.keySet()) {

            report.append(category)
                  .append(" : Rs. ")
                  .append(String.format("%.2f",
                      categoryExpenses.get(category)))
                  .append("\n");
        }

        report.append("\nTotal Income: Rs. ")
              .append(String.format("%.2f", getIncome()));

        report.append("\nTotal Expenses: Rs. ")
              .append(String.format("%.2f", getExpense()));

        report.append("\nRemaining Balance: Rs. ")
              .append(String.format("%.2f", getBalance()));

        return report.toString();
    }
}

// ==========================================
// 7. MULTITHREADING - AUTO SAVE
// ==========================================

class AutoSaveThread extends Thread {

    ExpenseManager manager;

    AutoSaveThread(ExpenseManager manager) {

        this.manager = manager;

        setDaemon(true);
        setName("AutoSaveThread");
    }

    @Override
    public void run() {

        while (true) {

            try {

                Thread.sleep(30000);

                manager.saveData();

            } catch (InterruptedException e) {

                break;
            }
        }
    }
}

// ==========================================
// 8. MAIN CLASS
// ==========================================

public class Main {

    static ExpenseManager manager = new ExpenseManager();

    public static void main(String[] args) {

        manager.loadData();

        new AutoSaveThread(manager).start();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}

// ==========================================
// 9. LOGIN FRAME
// ==========================================

class LoginFrame extends JFrame {

    LoginFrame() {

        setTitle("Personal Expense Tracker - Login");

        setSize(450, 350);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(235, 242, 255));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel(
            "PERSONAL EXPENSE TRACKER",
            JLabel.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 21));

        title.setForeground(new Color(20, 55, 110));

        JLabel userLabel = new JLabel("Username:");

        JTextField username = new JTextField(18);

        JLabel passLabel = new JLabel("Password:");

        JPasswordField password = new JPasswordField(18);

        JButton login = new JButton("LOGIN");

        login.setBackground(new Color(20, 70, 140));

        login.setForeground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;

        panel.add(userLabel, gbc);

        gbc.gridx = 1;

        panel.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(passLabel, gbc);

        gbc.gridx = 1;

        panel.add(password, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        panel.add(login, gbc);

        login.addActionListener(e -> {

            String user = username.getText();

            String pass = new String(
                password.getPassword()
            );

            if (user.equals("admin")
                    && pass.equals("admin123")) {

                JOptionPane.showMessageDialog(
                    this, "Login Successful!"
                );

                dispose();

                new DashboardFrame();

            } else {

                JOptionPane.showMessageDialog(
                    this, "Invalid Username or Password!"
                );
            }
        });

        add(panel);

        setVisible(true);
    }
}

// ==========================================
// 10. DASHBOARD FRAME
// ==========================================

class DashboardFrame extends JFrame {

    JLabel incomeLabel;
    JLabel expenseLabel;
    JLabel balanceLabel;

    JTable table;

    DefaultTableModel model;

    JTextField searchField;

    Color navy = new Color(20, 55, 105);

    Color lightBlue = new Color(235, 242, 255);

    DashboardFrame() {

        setTitle("Personal Expense Tracker");

        setSize(1150, 700);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // HEADER

        JLabel heading = new JLabel(
            "   PERSONAL EXPENSE TRACKER"
        );

        heading.setOpaque(true);

        heading.setBackground(navy);

        heading.setForeground(Color.WHITE);

        heading.setFont(
            new Font("Arial", Font.BOLD, 25)
        );

        heading.setPreferredSize(
            new Dimension(100, 65)
        );

        add(heading, BorderLayout.NORTH);

        // CENTER PANEL

        JPanel center = new JPanel(
            new BorderLayout(10, 10)
        );

        center.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        center.setBackground(lightBlue);

        // SUMMARY CARDS

        JPanel cards = new JPanel(
            new GridLayout(1, 3, 12, 12)
        );

        cards.setBackground(lightBlue);

        incomeLabel = new JLabel("", JLabel.CENTER);

        expenseLabel = new JLabel("", JLabel.CENTER);

        balanceLabel = new JLabel("", JLabel.CENTER);

        cards.add(createCard(
            "TOTAL INCOME",
            incomeLabel,
            new Color(210, 245, 220)
        ));

        cards.add(createCard(
            "TOTAL EXPENSES",
            expenseLabel,
            new Color(255, 220, 220)
        ));

        cards.add(createCard(
            "REMAINING BALANCE",
            balanceLabel,
            new Color(210, 230, 255)
        ));

        center.add(cards, BorderLayout.NORTH);

        // TRANSACTION TABLE

        String[] columns = {
            "ID",
            "Date",
            "Type",
            "Category",
            "Amount",
            "Description"
        };

        model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row, int column) {

                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(28);

        table.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );

        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 14)
        );

        JScrollPane scroll = new JScrollPane(table);

        JPanel tablePanel = new JPanel(
            new BorderLayout(5, 5)
        );

        tablePanel.setBackground(lightBlue);

        JLabel tableTitle = new JLabel(
            "Recent Transactions"
        );

        tableTitle.setFont(
            new Font("Arial", Font.BOLD, 18)
        );

        tablePanel.add(tableTitle, BorderLayout.NORTH);

        tablePanel.add(scroll, BorderLayout.CENTER);

        center.add(tablePanel, BorderLayout.CENTER);

        // SEARCH PANEL

        JPanel searchPanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        searchPanel.setBackground(lightBlue);

        searchField = new JTextField(18);

        JButton searchButton = new JButton("Search");

        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Search:"));

        searchPanel.add(searchField);

        searchPanel.add(searchButton);

        searchPanel.add(showAllButton);

        // ACTION BUTTONS

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 8)
        );

        buttons.setBackground(lightBlue);

        JButton addIncome = new JButton("Add Income");

        JButton addExpense = new JButton("Add Expense");

        JButton edit = new JButton("Edit");

        JButton delete = new JButton("Delete");

        JButton report = new JButton("Reports");

        JButton logout = new JButton("Logout");

        addIncome.setBackground(new Color(25, 150, 65));

        addIncome.setForeground(Color.WHITE);

        addExpense.setBackground(new Color(200, 45, 45));

        addExpense.setForeground(Color.WHITE);

        buttons.add(addIncome);

        buttons.add(addExpense);

        buttons.add(edit);

        buttons.add(delete);

        buttons.add(report);

        buttons.add(logout);

        JPanel bottom = new JPanel(
            new BorderLayout()
        );

        bottom.setBackground(lightBlue);

        bottom.add(searchPanel, BorderLayout.NORTH);

        bottom.add(buttons, BorderLayout.SOUTH);

        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // BUTTON EVENTS

        addIncome.addActionListener(e ->
            openForm("Income", null)
        );

        addExpense.addActionListener(e ->
            openForm("Expense", null)
        );

        searchButton.addActionListener(e ->
            refreshTable(searchField.getText().trim())
        );

        showAllButton.addActionListener(e -> {

            searchField.setText("");

            refreshTable("");
        });

        edit.addActionListener(e ->
            editSelected()
        );

        delete.addActionListener(e ->
            deleteSelected()
        );

        report.addActionListener(e ->
            showReport()
        );

        logout.addActionListener(e -> {

            Main.manager.saveData();

            dispose();

            new LoginFrame();
        });

        // MOUSE LISTENER - DOUBLE CLICK TO EDIT

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {

                    editSelected();
                }
            }
        });

        refreshTable("");

        // WINDOW CLOSE EVENT

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                Main.manager.saveData();

                dispose();

                System.exit(0);
            }
        });

        setVisible(true);
    }

    // ======================================
    // CREATE DASHBOARD CARD
    // ======================================

    JPanel createCard(
            String title, JLabel value, Color color) {

        JPanel panel = new JPanel(
            new GridLayout(2, 1, 5, 5)
        );

        panel.setBackground(color);

        panel.setBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        );

        JLabel heading = new JLabel(
            title, JLabel.CENTER
        );

        heading.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        value.setFont(
            new Font("Arial", Font.BOLD, 23)
        );

        panel.add(heading);

        panel.add(value);

        return panel;
    }

    // ======================================
    // UPDATE SUMMARY
    // ======================================

    void refreshSummary() {

        ExpenseManager m = Main.manager;

        incomeLabel.setText(
            "Rs. " + String.format("%.2f", m.getIncome())
        );

        expenseLabel.setText(
            "Rs. " + String.format("%.2f", m.getExpense())
        );

        balanceLabel.setText(
            "Rs. " + String.format("%.2f", m.getBalance())
        );
    }

    // ======================================
    // DISPLAY TRANSACTIONS
    // ======================================

    void refreshTable(String keyword) {

        model.setRowCount(0);

        ExpenseManager m = Main.manager;

        ArrayList<Transaction> list;

        if (keyword == null || keyword.isEmpty()) {

            synchronized (m) {
                list = new ArrayList<>(m.transactions);
            }

        } else {

            list = m.search(keyword);
        }

        for (Transaction t : list) {

            String amount =
                (t.getType().equals("Expense") ? "-" : "+")
                + String.format("%.2f", t.amount);

            model.addRow(new Object[] {
                t.id,
                t.date,
                t.getType(),
                t.category,
                amount,
                t.description
            });
        }

        refreshSummary();
    }

    // ======================================
    // ADD / EDIT TRANSACTION FORM
    // ======================================

    void openForm(String type, Transaction old) {

        JTextField dateField = new JTextField(
            old == null
                ? LocalDate.now().toString()
                : old.date
        );

        String[] categories;

        if (type.equals("Income")) {

            categories = new String[] {
                "Salary",
                "Pocket Money",
                "Business",
                "Other"
            };

        } else {

            categories = new String[] {
                "Food",
                "Travel",
                "Shopping",
                "Education",
                "Entertainment",
                "Bills",
                "Health",
                "Other"
            };
        }

        JComboBox<String> categoryBox =
            new JComboBox<>(categories);

        if (old != null) {
            categoryBox.setSelectedItem(old.category);
        }

        JTextField amountField = new JTextField(
            old == null ? "" : String.valueOf(old.amount)
        );

        JTextField descriptionField = new JTextField(
            old == null ? "" : old.description
        );

        JPanel form = new JPanel(
            new GridLayout(0, 2, 10, 10)
        );

        form.add(new JLabel("Transaction Type:"));

        form.add(new JLabel(type));

        form.add(new JLabel("Date (YYYY-MM-DD):"));

        form.add(dateField);

        form.add(new JLabel("Category:"));

        form.add(categoryBox);

        form.add(new JLabel("Amount (Rs.):"));

        form.add(amountField);

        form.add(new JLabel("Description:"));

        form.add(descriptionField);

        int option = JOptionPane.showConfirmDialog(
            this,
            form,
            old == null ? "Add " + type : "Edit Transaction",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {

            try {

                String date = dateField.getText().trim();

                // Validate date
                LocalDate.parse(date);

                double amount = Double.parseDouble(
                    amountField.getText().trim()
                );

                String category =
                    (String) categoryBox.getSelectedItem();

                String description =
                    descriptionField.getText().trim();

                if (description.isEmpty()) {

                    throw new IllegalArgumentException(
                        "Please enter a description."
                    );
                }

                if (old == null) {

                    Main.manager.addTransaction(
                        type,
                        date,
                        category,
                        amount,
                        description
                    );

                } else {

                    Main.manager.updateTransaction(
                        old.id,
                        type,
                        date,
                        category,
                        amount,
                        description
                    );
                }

                Main.manager.saveData();

                refreshTable("");

                JOptionPane.showMessageDialog(
                    this,
                    "Transaction saved successfully!"
                );

            // CORRECTED CATCH BLOCK
            } catch (InvalidAmountException |
                     IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                    this,
                    "Invalid details: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid date in YYYY-MM-DD format.",
                    "Date Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // ======================================
    // EDIT SELECTED TRANSACTION
    // ======================================

    void editSelected() {

        int row = table.getSelectedRow();

        if (row == -1) {

            JOptionPane.showMessageDialog(
                this,
                "Please select a transaction first!"
            );

            return;
        }

        int id = Integer.parseInt(
            model.getValueAt(row, 0).toString()
        );

        synchronized (Main.manager) {

            for (Transaction t : Main.manager.transactions) {

                if (t.id == id) {

                    openForm(t.getType(), t);

                    return;
                }
            }
        }
    }

    // ======================================
    // DELETE SELECTED TRANSACTION
    // ======================================

    void deleteSelected() {

        int row = table.getSelectedRow();

        if (row == -1) {

            JOptionPane.showMessageDialog(
                this,
                "Please select a transaction first!"
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this transaction?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            int id = Integer.parseInt(
                model.getValueAt(row, 0).toString()
            );

            Main.manager.deleteTransaction(id);

            Main.manager.saveData();

            refreshTable("");
        }
    }

    // ======================================
    // SHOW EXPENSE REPORT
    // ======================================

    void showReport() {

        String report =
            Main.manager.generateReport();

        JTextArea area = new JTextArea(
            report, 15, 35
        );

        area.setEditable(false);

        area.setFont(
            new Font("Monospaced", Font.PLAIN, 14)
        );

        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(area),
            "Expense Report",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}