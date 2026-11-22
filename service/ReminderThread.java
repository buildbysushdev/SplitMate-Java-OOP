package service;

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

// Practical 8: MULTITHREADING - thread 2 shows a payment reminder one by one
public class ReminderThread extends Thread {
    private ExpenseManager manager;
    private JLabel label;
    private int seconds;

    public ReminderThread(ExpenseManager manager, JLabel label, int seconds) {
        this.manager = manager;
        this.label = label;
        this.seconds = seconds;
        setDaemon(true);
    }

    public void run() {
        int i = 0;
        while (true) {
            ArrayList<String> list = manager.settlements();
            final String text;
            if (list.isEmpty())
                text = "Reminder: everyone is settled up!";
            else
                text = "Reminder: " + list.get(i % list.size());
            i++;
            SwingUtilities.invokeLater(() -> label.setText(text));
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
