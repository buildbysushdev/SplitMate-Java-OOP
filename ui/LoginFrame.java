package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Practical 10: SWING login form      Practical 11: ActionListener + MouseListener
public class LoginFrame extends JFrame implements ActionListener, MouseListener {
    JTextField user = new JTextField(15);
    JPasswordField pass = new JPasswordField(15);
    JButton login = new JButton("Login");
    JLabel msg = new JLabel(" ");

    public LoginFrame() {
        super("SplitMate - Login");
        setLayout(new FlowLayout());
        JLabel title = new JLabel("SplitMate");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0, 121, 107));
        JLabel tag = new JLabel("Spend together, settle easily.");
        user.setName("username");
        pass.setName("password");
        login.setName("login");
        msg.setForeground(Color.RED);
        add(title);
        add(tag);
        add(new JLabel("Username:"));
        add(user);
        add(new JLabel("Password:"));
        add(pass);
        add(login);
        add(msg);
        login.addActionListener(this);      // button click
        login.addMouseListener(this);       // mouse over the button
        setSize(300, 270);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String u = user.getText();
        String p = new String(pass.getPassword());
        if (u.equals("sreeram") && p.equals("1234")) {
            new Dashboard(u);
            dispose();
        } else {
            msg.setText("Invalid username or password");
        }
    }

    public void mouseEntered(MouseEvent e) { login.setBackground(new Color(178, 223, 219)); }
    public void mouseExited(MouseEvent e) { login.setBackground(null); }
    public void mouseClicked(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
}
