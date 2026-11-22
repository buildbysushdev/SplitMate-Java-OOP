package service;

import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

// Practical 8: MULTITHREADING - thread 1 saves the data again and again
public class AutoSaveThread extends Thread {
    private ExpenseManager manager;
    private String file;
    private JLabel status;
    private int seconds;

    public AutoSaveThread(ExpenseManager manager, String file, JLabel status, int seconds) {
        this.manager = manager;
        this.file = file;
        this.status = status;
        this.seconds = seconds;
        setDaemon(true);                    // stops automatically when the window closes
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                return;
            }
            try {
                manager.saveToFile(file);
                final String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                SwingUtilities.invokeLater(() -> status.setText("Auto-saved at " + time));
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> status.setText("Auto-save failed"));
            }
        }
    }
}
