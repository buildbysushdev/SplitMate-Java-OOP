package ui;

import java.awt.*;
import javax.swing.*;

// Practical 10: GUI - the login window
public class LoginFrame extends JFrame {
    private static final String USER = "sreeram";
    private static final String PASS = "1234";
    private static final Color TEAL = new Color(0, 128, 114);

    private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JLabel message = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        super("SplitMate - Login");
        setSize(440, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(232, 245, 243));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SPLITMATE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEAL);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        root.add(title, c);

        JLabel tag = new JLabel("Spend together, settle easily.", SwingConstants.CENTER);
        tag.setFont(new Font("SansSerif", Font.ITALIC, 13));
        tag.setForeground(new Color(70, 90, 88));
        c.gridy = 1;
        c.insets = new Insets(0, 10, 18, 10);
        root.add(tag, c);

        c.insets = new Insets(8, 10, 8, 10);
        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0; c.weightx = 0;
        root.add(bold("Username:"), c);
        c.gridx = 1; c.weightx = 1;
        root.add(userField, c);

        c.gridy = 3; c.gridx = 0; c.weightx = 0;
        root.add(bold("Password:"), c);
        c.gridx = 1; c.weightx = 1;
        root.add(passField, c);

        JButton login = new JButton("LOGIN");
        login.setFont(new Font("SansSerif", Font.BOLD, 14));
        login.setBackground(TEAL);
        login.setForeground(Color.WHITE);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        c.insets = new Insets(16, 10, 6, 10);
        root.add(login, c);

        message.setForeground(Color.RED);
        c.gridy = 5;
        c.insets = new Insets(2, 10, 2, 10);
        root.add(message, c);

        setContentPane(root);
        getRootPane().setDefaultButton(login);          // Enter key = click LOGIN

        login.addActionListener(e -> tryLogin());
    }

    private JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private void tryLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.equals(USER) && p.equals(PASS)) {
            dispose();
            new Dashboard(u).setVisible(true);
        } else {
            message.setText("Wrong username or password");
            passField.setText("");
        }
    }
}
