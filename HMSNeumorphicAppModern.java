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

     // ------------------- Add Patient -------------------
    private void addPatientDialog() {
        JDialog dialog = new JDialog(this, "Add Patient", true);
        dialog.setSize(400, 500);
        dialog.setLayout(new GridLayout(11, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField bloodField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField emergencyField = new JTextField();
        JTextField medicalField = new JTextField();
        JTextField allergyField = new JTextField();

        dialog.add(new JLabel("Full Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Age:")); dialog.add(ageField);
        dialog.add(new JLabel("Gender:")); dialog.add(genderField);
        dialog.add(new JLabel("Blood Type:")); dialog.add(bloodField);
        dialog.add(new JLabel("Phone:")); dialog.add(phoneField);
        dialog.add(new JLabel("Email:")); dialog.add(emailField);
        dialog.add(new JLabel("Address:")); dialog.add(addressField);
        dialog.add(new JLabel("Emergency Contact:")); dialog.add(emergencyField);
        dialog.add(new JLabel("Medical History:")); dialog.add(medicalField);
        dialog.add(new JLabel("Allergies:")); dialog.add(allergyField);

        JButton saveBtn = new JButton("Save");
        applyNeumorphicButton(saveBtn);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String age = ageField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || age.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Age are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidAge(age)) {
                JOptionPane.showMessageDialog(dialog, "Age must be between 1 and 150!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!phone.isEmpty() && !isValidGhanaPhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Phone must be 10 digits starting with 02, 03, 04, or 05!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.isEmpty() && !isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isDuplicatePatient(name, phone)) {
                JOptionPane.showMessageDialog(dialog, "Patient with this name and phone already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient p = new Patient(
                    name, age, genderField.getText(), bloodField.getText(),
                    phone, email, addressField.getText(), emergencyField.getText(),
                    medicalField.getText(), allergyField.getText()
                    
            );
            patients.add(p);
            updatePatientsTable(doctorPatientsModel);
            if (adminPatientsModel != null) updatePatientsTable(adminPatientsModel);
            JOptionPane.showMessageDialog(dialog, "Patient added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        dialog.add(new JLabel()); dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void updatePatientsTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (Patient p : patients) {
            model.addRow(new Object[]{p.name, p.age, p.gender, p.bloodType, p.phone, p.email});
        }
    }
}