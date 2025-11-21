import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

// Hospital Management System - Modern UI with Database
public class HMSNeumorphicAppModern extends JFrame {

    private String userRole;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private javax.swing.Timer notificationTimer;
    private DatabaseManager db;

    private List<Patient> patients = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();
    private List<Bill> bills = new ArrayList<>();
    private List<Service> services = new ArrayList<>();

    private JTable adminPatientsTable;
    private JTable adminAppointmentsTable;
    private JTable adminDoctorsTable;
    private JTable adminBillingTable;
    private JTable doctorPatientsTable;
    private JTable doctorAppointmentsTable;

    private DefaultTableModel adminPatientsModel;
    private DefaultTableModel adminAppointmentsModel;
    private DefaultTableModel adminDoctorsModel;
    private DefaultTableModel adminBillingModel;
    private DefaultTableModel doctorPatientsModel;
    private DefaultTableModel doctorAppointmentsModel;

    // Color Scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);

    public HMSNeumorphicAppModern() {
        setTitle("Hospital Management System");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize database
        db = new DatabaseManager();
        loadDataFromDatabase();

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel, BorderLayout.CENTER);

        initServices();
        initLoginPage();
        initAdminDashboard();
        initDoctorDashboard();
        setupNotificationTimer();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (db != null) {
                    db.close();
                }
            }
        });

        setVisible(true);
    }

    private void loadDataFromDatabase() {
        patients = db.loadPatients();
        doctors = db.loadDoctors();
        appointments = db.loadAppointments();
        bills = db.loadBills();
    }

    private void setupNotificationTimer() {
        notificationTimer = new javax.swing.Timer(300000, e -> checkNotifications());
        notificationTimer.start();
    }

    private void checkNotifications() {
        if (appointments.isEmpty() || userRole == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date today = new java.util.Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, 1);
        java.util.Date tomorrow = cal.getTime();

        int todayCount = 0;
        int tomorrowCount = 0;

        for (Appointment appt : appointments) {
            try {
                java.util.Date apptDate = sdf.parse(appt.date);
                String todayStr = sdf.format(today);
                String tomorrowStr = sdf.format(tomorrow);
                String apptStr = sdf.format(apptDate);

                if (apptStr.equals(todayStr)) todayCount++;
                if (apptStr.equals(tomorrowStr)) tomorrowCount++;
            } catch (ParseException ex) {}
        }

        if (todayCount > 0 || tomorrowCount > 0) {
            StringBuilder msg = new StringBuilder();
            if (todayCount > 0) msg.append("Today: ").append(todayCount).append(" appointment(s)\n");
            if (tomorrowCount > 0) msg.append("Tomorrow: ").append(tomorrowCount).append(" appointment(s)");
            
            JOptionPane.showMessageDialog(this, msg.toString(), "Appointment Reminder", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void initServices() {
        services.add(new Service("General Consultation", 150));
        services.add(new Service("Blood Test", 75));
        services.add(new Service("X-Ray", 200));
        services.add(new Service("ECG", 100));
        services.add(new Service("Ultrasound", 250));
        services.add(new Service("Surgery", 1500));
        services.add(new Service("Physiotherapy", 180));
    }

    private void initLoginPage() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel loginCard = new JPanel();
        loginCard.setBackground(CARD_COLOR);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        loginCard.setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("Hospital Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        styleTextField(userField);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        styleTextField(passField);

        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Administrator", "Doctor"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        styleComboBox(roleCombo);

        JButton loginBtn = createStyledButton("Login", PRIMARY_COLOR);
        loginBtn.setPreferredSize(new Dimension(200, 40));

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean valid = false;
            if (role.equals("Administrator") && username.equals("admin") && password.equals("admin123")) {
                valid = true;
            } else if (role.equals("Doctor") && username.equals("doctor") && password.equals("doctor123")) {
                valid = true;
            }

            if (!valid) {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            userRole = role;
            if (role.equals("Administrator")) {
                refreshAdminDashboard();
                cardLayout.show(mainPanel, "admin");
            } else {
                refreshDoctorDashboard();
                cardLayout.show(mainPanel, "doctor");
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginCard.add(titleLabel, gbc);
        
        gbc.gridwidth = 1; gbc.gridy++;
        loginCard.add(userLabel, gbc);
        gbc.gridx = 1;
        loginCard.add(userField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        loginCard.add(passLabel, gbc);
        gbc.gridx = 1;
        loginCard.add(passField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        loginCard.add(roleLabel, gbc);
        gbc.gridx = 1;
        loginCard.add(roleCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        loginCard.add(loginBtn, gbc);

        loginPanel.add(loginCard);
        mainPanel.add(loginPanel, "login");
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(new Color(250, 250, 250));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void initAdminDashboard() {
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Administrator Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JButton logoutBtn = createStyledButton("Logout", new Color(120, 120, 120));
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        adminPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabbedPane.add("Patients", createPatientsTab(true));
        tabbedPane.add("Appointments", createAppointmentsTab(true));
        tabbedPane.add("Doctors", createDoctorsTab());
        tabbedPane.add("Billing", createBillingTab());

        adminPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(adminPanel, "admin");
    }

    private JPanel createPatientsTab(boolean isAdmin) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Name", "Age", "Gender", "Blood Type", "Phone", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        if (isAdmin) {
            adminPatientsModel = model;
            adminPatientsTable = table;
        } else {
            doctorPatientsModel = model;
            doctorPatientsTable = table;
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        if (!isAdmin) {
            JButton addBtn = createStyledButton("Add Patient", ACCENT_COLOR);
            addBtn.addActionListener(e -> showAddPatientDialog());
            topPanel.add(addBtn, BorderLayout.WEST);
        }
        
        JPanel searchPanel = createSearchPanel(table, model);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createAppointmentsTab(boolean isAdmin) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Patient", "Doctor", "Type", "Date", "Time", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        if (isAdmin) {
            adminAppointmentsModel = model;
            adminAppointmentsTable = table;
        } else {
            doctorAppointmentsModel = model;
            doctorAppointmentsTable = table;
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        if (!isAdmin) {
            JButton addBtn = createStyledButton("Schedule Appointment", ACCENT_COLOR);
            addBtn.addActionListener(e -> showAddAppointmentDialog());
            topPanel.add(addBtn, BorderLayout.WEST);
        }
        
        JPanel searchPanel = createSearchPanel(table, model);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createDoctorsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Name", "Specialty", "Phone"};
        adminDoctorsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        adminDoctorsTable = new JTable(adminDoctorsModel);
        styleTable(adminDoctorsTable);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        JButton addBtn = createStyledButton("Add Doctor", ACCENT_COLOR);
        addBtn.addActionListener(e -> showAddDoctorDialog());
        
        JPanel searchPanel = createSearchPanel(adminDoctorsTable, adminDoctorsModel);
        
        topPanel.add(addBtn, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(adminDoctorsTable), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBillingTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Patient", "Services", "Total"};
        adminBillingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        adminBillingTable = new JTable(adminBillingModel);
        styleTable(adminBillingTable);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        JButton addBtn = createStyledButton("Create Bill", ACCENT_COLOR);
        addBtn.addActionListener(e -> showCreateBillDialog());
        
        JPanel searchPanel = createSearchPanel(adminBillingTable, adminBillingModel);
        
        topPanel.add(addBtn, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(adminBillingTable), BorderLayout.CENTER);
        
        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(220, 240, 255));
    }

    private JPanel createSearchPanel(JTable table, DefaultTableModel model) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);

        JTextField searchField = new JTextField(15);
        styleTextField(searchField);
        
        JButton searchBtn = createStyledButton("Search", PRIMARY_COLOR);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchBtn.addActionListener(e -> {
            String text = searchField.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchBtn);

        return panel;
    }

    private void refreshAdminDashboard() {
        updatePatientsTable(adminPatientsModel);
        updateAppointmentsTable(adminAppointmentsModel);
        updateDoctorsTable();
        updateBillingTable();
    }

    private void refreshDoctorDashboard() {
        updatePatientsTable(doctorPatientsModel);
        updateAppointmentsTable(doctorAppointmentsModel);
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog(this, "Add Doctor", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField specialtyField = new JTextField();
        JTextField phoneField = new JTextField();

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Specialty:"));
        dialog.add(specialtyField);
        dialog.add(new JLabel("Phone:"));
        dialog.add(phoneField);

        JButton saveBtn = createStyledButton("Save", ACCENT_COLOR);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String specialty = specialtyField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || specialty.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!");
                return;
            }

            if (!isValidGhanaPhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Invalid Ghana phone number!");
                return;
            }

            Doctor doctor = new Doctor(name, specialty, phone);
            doctors.add(doctor);
            db.saveDoctor(doctor);
            updateDoctorsTable();
            JOptionPane.showMessageDialog(dialog, "Doctor added successfully!");
            dialog.dispose();
        });

        dialog.add(new JLabel());
        dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void showAddPatientDialog() {
        JDialog dialog = new JDialog(this, "Add Patient", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(11, 2, 5, 5));

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

        dialog.add(new JLabel("Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Age:")); dialog.add(ageField);
        dialog.add(new JLabel("Gender:")); dialog.add(genderField);
        dialog.add(new JLabel("Blood Type:")); dialog.add(bloodField);
        dialog.add(new JLabel("Phone:")); dialog.add(phoneField);
        dialog.add(new JLabel("Email:")); dialog.add(emailField);
        dialog.add(new JLabel("Address:")); dialog.add(addressField);
        dialog.add(new JLabel("Emergency Contact:")); dialog.add(emergencyField);
        dialog.add(new JLabel("Medical History:")); dialog.add(medicalField);
        dialog.add(new JLabel("Allergies:")); dialog.add(allergyField);

        JButton saveBtn = createStyledButton("Save", ACCENT_COLOR);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String age = ageField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || age.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Age are required!");
                return;
            }

            if (!isValidAge(age)) {
                JOptionPane.showMessageDialog(dialog, "Invalid age!");
                return;
            }

            if (!phone.isEmpty() && !isValidGhanaPhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Invalid Ghana phone number!");
                return;
            }

            if (!email.isEmpty() && !isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Invalid email!");
                return;
            }

            Patient patient = new Patient(name, age, genderField.getText(), bloodField.getText(),
                    phone, email, addressField.getText(), emergencyField.getText(),
                    medicalField.getText(), allergyField.getText());
            
            patients.add(patient);
            db.savePatient(patient);
            updatePatientsTable(doctorPatientsModel);
            if (adminPatientsModel != null) updatePatientsTable(adminPatientsModel);
            JOptionPane.showMessageDialog(dialog, "Patient added successfully!");
            dialog.dispose();
        });

        dialog.add(new JLabel());
        dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void showAddAppointmentDialog() {
        if (patients.isEmpty() || doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients or doctors available!");
            return;
        }

        JDialog dialog = new JDialog(this, "Schedule Appointment", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : patients) patientCombo.addItem(p.name);

        JComboBox<String> doctorCombo = new JComboBox<>();
        for (Doctor d : doctors) doctorCombo.addItem(d.name);

        String[] types = {"Consultation", "Follow-up", "Emergency", "Surgery"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        JTextField dateField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField notesField = new JTextField();

        dialog.add(new JLabel("Patient:")); dialog.add(patientCombo);
        dialog.add(new JLabel("Doctor:")); dialog.add(doctorCombo);
        dialog.add(new JLabel("Type:")); dialog.add(typeCombo);
        dialog.add(new JLabel("Date (DD/MM/YYYY):")); dialog.add(dateField);
        dialog.add(new JLabel("Time (HH:MM):")); dialog.add(timeField);
        dialog.add(new JLabel("Notes:")); dialog.add(notesField);

        JButton saveBtn = createStyledButton("Save", ACCENT_COLOR);
        saveBtn.addActionListener(e -> {
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();

            if (date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Date and Time are required!");
                return;
            }

            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(dialog, "Invalid date!");
                return;
            }

            if (!isValidTime(time)) {
                JOptionPane.showMessageDialog(dialog, "Invalid time!");
                return;
            }

            Appointment appointment = new Appointment(
                (String) patientCombo.getSelectedItem(),
                (String) doctorCombo.getSelectedItem(),
                (String) typeCombo.getSelectedItem(),
                date, time, notesField.getText()
            );
            
            appointments.add(appointment);
            db.saveAppointment(appointment);
            updateAppointmentsTable(doctorAppointmentsModel);
            if (adminAppointmentsModel != null) updateAppointmentsTable(adminAppointmentsModel);
            JOptionPane.showMessageDialog(dialog, "Appointment scheduled!");
            dialog.dispose();
        });

        dialog.add(new JLabel());
        dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void showCreateBillDialog() {
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients available!");
            return;
        }

        JDialog dialog = new JDialog(this, "Create Bill", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : patients) patientCombo.addItem(p.name);

        JList<String> servicesList = new JList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Service s : services) {
            listModel.addElement(s.name + " - $" + s.price);
        }
        servicesList.setModel(listModel);
        servicesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        formPanel.add(new JLabel("Patient:"));
        formPanel.add(patientCombo);
        formPanel.add(new JLabel("Services:"));
        formPanel.add(new JScrollPane(servicesList));

        JButton saveBtn = createStyledButton("Generate Bill", ACCENT_COLOR);
        saveBtn.addActionListener(e -> {
            List<String> selected = servicesList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Select at least one service!");
                return;
            }

            List<Service> selectedServices = new ArrayList<>();
            double total = 0;
            for (String serviceStr : selected) {
                for (Service s : services) {
                    if (serviceStr.contains(s.name)) {
                        selectedServices.add(s);
                        total += s.price;
                        break;
                    }
                }
            }

            Bill bill = new Bill((String) patientCombo.getSelectedItem(), selectedServices, total);
            bills.add(bill);
            db.saveBill(bill);
            updateBillingTable();
            JOptionPane.showMessageDialog(dialog, "Bill created! Total: $" + total);
            dialog.dispose();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updatePatientsTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (Patient p : patients) {
            model.addRow(new Object[]{p.name, p.age, p.gender, p.bloodType, p.phone, p.email});
        }
    }

    private void updateAppointmentsTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (Appointment a : appointments) {
            model.addRow(new Object[]{a.patient, a.doctor, a.type, a.date, a.time, a.notes});
        }
    }

    private void updateDoctorsTable() {
        adminDoctorsModel.setRowCount(0);
        for (Doctor d : doctors) {
            adminDoctorsModel.addRow(new Object[]{d.name, d.specialty, d.phone});
        }
    }

    private void updateBillingTable() {
        adminBillingModel.setRowCount(0);
        for (Bill b : bills) {
            StringBuilder servicesStr = new StringBuilder();
            for (int i = 0; i < b.services.size(); i++) {
                servicesStr.append(b.services.get(i).name);
                if (i < b.services.size() - 1) servicesStr.append(", ");
            }
            adminBillingModel.addRow(new Object[]{b.patientName, servicesStr.toString(), "$" + b.total});
        }
    }

    private void initDoctorDashboard() {
        JPanel doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Doctor Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JButton logoutBtn = createStyledButton("Logout", new Color(120, 120, 120));
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        doctorPanel.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabbedPane.add("Patients", createPatientsTab(false));
        tabbedPane.add("Appointments", createAppointmentsTab(false));

        doctorPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(doctorPanel, "doctor");
    }

    // Validation methods
    private boolean isValidGhanaPhone(String phone) {
        return phone.matches("^(02|03|04|05)\\d{8}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }

    private boolean isValidAge(String age) {
        try {
            int ageInt = Integer.parseInt(age);
            return ageInt > 0 && ageInt <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        return time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HMSNeumorphicAppModern());
    }

    // Data classes
    static class Patient {
        String name, age, gender, bloodType, phone, email, address, emergency, medicalHistory, allergies;
        
        public Patient(String name, String age, String gender, String bloodType, String phone, 
                      String email, String address, String emergency, String medicalHistory, String allergies) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.bloodType = bloodType;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.emergency = emergency;
            this.medicalHistory = medicalHistory;
            this.allergies = allergies;
        }
    }

    static class Doctor {
        String name, specialty, phone;
        public Doctor(String name, String specialty, String phone) {
            this.name = name;
            this.specialty = specialty;
            this.phone = phone;
        }
    }

    static class Appointment {
        String patient, doctor, type, date, time, notes;
        public Appointment(String patient, String doctor, String type, String date, String time, String notes) {
            this.patient = patient;
            this.doctor = doctor;
            this.type = type;
            this.date = date;
            this.time = time;
            this.notes = notes;
        }
    }

    static class Service {
        String name;
        double price;
        public Service(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }

    static class Bill {
        String patientName;
        List<Service> services;
        double total;
        public Bill(String patientName, List<Service> services, double total) {
            this.patientName = patientName;
            this.services = services;
            this.total = total;
        }
    }
}

// Database Manager
class DatabaseManager {
    private Connection conn;
    
    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:hospital_data.db");
            createTables();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }
    
    private void createTables() throws SQLException {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS patients (id INTEGER PRIMARY KEY, name TEXT, age TEXT, gender TEXT, blood_type TEXT, phone TEXT, email TEXT, address TEXT, emergency_contact TEXT, medical_history TEXT, allergies TEXT)",
            "CREATE TABLE IF NOT EXISTS doctors (id INTEGER PRIMARY KEY, name TEXT, specialty TEXT, phone TEXT)",
            "CREATE TABLE IF NOT EXISTS appointments (id INTEGER PRIMARY KEY, patient_name TEXT, doctor_name TEXT, type TEXT, date TEXT, time TEXT, notes TEXT)",
            "CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY, patient_name TEXT, services TEXT, total REAL)"
        };
        
        for (String sql : tables) {
            conn.createStatement().execute(sql);
        }
    }
    
    public void savePatient(HMSNeumorphicAppModern.Patient p) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO patients (name, age, gender, blood_type, phone, email, address, emergency_contact, medical_history, allergies) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, p.name);
            stmt.setString(2, p.age);
            stmt.setString(3, p.gender);
            stmt.setString(4, p.bloodType);
            stmt.setString(5, p.phone);
            stmt.setString(6, p.email);
            stmt.setString(7, p.address);
            stmt.setString(8, p.emergency);
            stmt.setString(9, p.medicalHistory);
            stmt.setString(10, p.allergies);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<HMSNeumorphicAppModern.Patient> loadPatients() {
        List<HMSNeumorphicAppModern.Patient> patients = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients")) {
            while (rs.next()) {
                patients.add(new HMSNeumorphicAppModern.Patient(
                    rs.getString("name"), rs.getString("age"), rs.getString("gender"),
                    rs.getString("blood_type"), rs.getString("phone"), rs.getString("email"),
                    rs.getString("address"), rs.getString("emergency_contact"),
                    rs.getString("medical_history"), rs.getString("allergies")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }
    
    public void saveDoctor(HMSNeumorphicAppModern.Doctor d) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO doctors (name, specialty, phone) VALUES (?,?,?)")) {
            stmt.setString(1, d.name);
            stmt.setString(2, d.specialty);
            stmt.setString(3, d.phone);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<HMSNeumorphicAppModern.Doctor> loadDoctors() {
        List<HMSNeumorphicAppModern.Doctor> doctors = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM doctors")) {
            while (rs.next()) {
                doctors.add(new HMSNeumorphicAppModern.Doctor(
                    rs.getString("name"), rs.getString("specialty"), rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }
    
    public void saveAppointment(HMSNeumorphicAppModern.Appointment a) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO appointments (patient_name, doctor_name, type, date, time, notes) VALUES (?,?,?,?,?,?)")) {
            stmt.setString(1, a.patient);
            stmt.setString(2, a.doctor);
            stmt.setString(3, a.type);
            stmt.setString(4, a.date);
            stmt.setString(5, a.time);
            stmt.setString(6, a.notes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<HMSNeumorphicAppModern.Appointment> loadAppointments() {
        List<HMSNeumorphicAppModern.Appointment> appointments = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM appointments")) {
            while (rs.next()) {
                appointments.add(new HMSNeumorphicAppModern.Appointment(
                    rs.getString("patient_name"), rs.getString("doctor_name"), rs.getString("type"),
                    rs.getString("date"), rs.getString("time"), rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    public void saveBill(HMSNeumorphicAppModern.Bill b) {
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO bills (patient_name, services, total) VALUES (?,?,?)")) {
            StringBuilder servicesStr = new StringBuilder();
            for (HMSNeumorphicAppModern.Service s : b.services) {
                servicesStr.append(s.name).append(":$").append(s.price).append(",");
            }
            stmt.setString(1, b.patientName);
            stmt.setString(2, servicesStr.toString());
            stmt.setDouble(3, b.total);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<HMSNeumorphicAppModern.Bill> loadBills() {
        List<HMSNeumorphicAppModern.Bill> bills = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM bills")) {
            while (rs.next()) {
                String servicesStr = rs.getString("services");
                List<HMSNeumorphicAppModern.Service> services = new ArrayList<>();
                if (servicesStr != null) {
                    String[] parts = servicesStr.split(",");
                    for (String part : parts) {
                        String[] serviceParts = part.split(":\\$");
                        if (serviceParts.length == 2) {
                            services.add(new HMSNeumorphicAppModern.Service(
                                serviceParts[0], Double.parseDouble(serviceParts[1])
                            ));
                        }
        }
                }
                bills.add(new HMSNeumorphicAppModern.Bill(
                    rs.getString("patient_name"), services, rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
