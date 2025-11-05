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
}