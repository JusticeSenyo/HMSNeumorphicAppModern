import javax.swing.*;
import java.awt.*;

// Hospital Appointment Scheduler - Stage 1: Project Foundation
public class HMSNeumorphicAppModern extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public HMSNeumorphicAppModern() {
        setTitle("Hospital Appointment Scheduler - Neumorphic Design");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        // Placeholder panels
        JPanel loginPanel = new JPanel();
        loginPanel.add(new JLabel("Login Screen - Coming Soon"));
        mainPanel.add(loginPanel, "login");

        JPanel adminPanel = new JPanel();
        adminPanel.add(new JLabel("Admin Dashboard - Coming Soon"));
        mainPanel.add(adminPanel, "admin");

        JPanel doctorPanel = new JPanel();
        doctorPanel.add(new JLabel("Doctor Dashboard - Coming Soon"));
        mainPanel.add(doctorPanel, "doctor");

        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HMSNeumorphicAppModern::new);
    }


// ------------------- Notification Timer -------------------
       private void setupNotificationTimer() {
        notificationTimer = new javax.swing.Timer(300_000, e -> checkNotifications()); // Every 5 minutes
        notificationTimer.setInitialDelay(5000); // Check 5 seconds after login
        notificationTimer.start();
    }

    private void checkNotifications() {
        if (appointments.isEmpty() || userRole == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        int todayCount = 0;
        int tomorrowCount = 0;

        for (Appointment appt : appointments) {
            try {
                Date apptDate = sdf.parse(appt.date);
                String todayStr = sdf.format(today);
                String tomorrowStr = sdf.format(tomorrow);
                String apptStr = sdf.format(apptDate);

                if (apptStr.equals(todayStr)) {
                    todayCount++;
                } else if (apptStr.equals(tomorrowStr)) {
                    tomorrowCount++;
                }
            } catch (ParseException ex) {
                // Skip invalid dates
            }
        }

        if (todayCount > 0 || tomorrowCount > 0) {
            StringBuilder msg = new StringBuilder();
            if (todayCount > 0) {
                msg.append("Today: ").append(todayCount).append(" appointment(s)\n");
            }
            if (tomorrowCount > 0) {
                msg.append("Tomorrow: ").append(tomorrowCount).append(" appointment(s)");
            }
            JOptionPane.showMessageDialog(this, msg.toString(),
                    "Appointment Reminder", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    

}