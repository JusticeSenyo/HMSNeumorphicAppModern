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

// Hospital Appointment Scheduler - Stage 16: SQLite Database Integration
public class HMSNeumorphicAppModern extends JFrame {

    private String userRole;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private javax.swing.Timer notificationTimer;
    private DatabaseManager db; // DATABASE MANAGER

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

    public HMSNeumorphicAppModern() {
        setTitle("Hospital Appointment Scheduler - Neumorphic Design");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize database and load data
        db = new DatabaseManager();
        loadDataFromDatabase();

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        initServices();
        initLoginPage();
        initAdminDashboard();
        initDoctorDashboard();
        setupNotificationTimer();

        // Close database on window close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                db.close();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    // Load all data from database
    private void loadDataFromDatabase() {
        patients = db.loadPatients();
        doctors = db.loadDoctors();
        appointments = db.loadAppointments();
        bills = db.loadBills();
        System.out.println("=== Data loaded from database ===");
    }

    private void setupNotificationTimer() {
        notificationTimer = new javax.swing.Timer(300_000, e -> checkNotifications());
        notificationTimer.setInitialDelay(5000);
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

                if (apptStr.equals(todayStr)) {
                    todayCount++;
                } else if (apptStr.equals(tomorrowStr)) {
                    tomorrowCount++;
                }
            } catch (ParseException ex) {
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
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(235, 235, 235));
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Hospital Appointment Scheduler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 70, 70));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);

        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Administrator", "Doctor"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        JButton loginBtn = new JButton("Login");
        applyNeumorphicButton(loginBtn);

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
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
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
        loginPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.gridy++;
        loginPanel.add(userLabel, gbc); gbc.gridx = 1; loginPanel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        loginPanel.add(passLabel, gbc); gbc.gridx = 1; loginPanel.add(passField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        loginPanel.add(roleLabel, gbc); gbc.gridx = 1; loginPanel.add(roleCombo, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        loginPanel.add(loginBtn, gbc);

        mainPanel.add(loginPanel, "login");
        cardLayout.show(mainPanel, "login");
    }

    private void applyNeumorphicButton(JButton button) {
        button.setBackground(new Color(225, 225, 225));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(50, 50, 50));
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private void initAdminDashboard() {
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(new Color(235, 235, 235));

        JLabel label = new JLabel("Administrator Dashboard");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton logoutBtn = new JButton("Logout");
        applyNeumorphicButton(logoutBtn);
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(235, 235, 235));
        topPanel.add(label, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        adminPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Patients", createPatientsTab(true));
        tabbedPane.add("Appointments", createAppointmentsTab(true));

        JPanel doctorsTab = new JPanel(new BorderLayout());
        JButton addDoctorBtn = new JButton("Add Doctor");
        applyNeumorphicButton(addDoctorBtn);
        addDoctorBtn.addActionListener(e -> addDoctorDialog());

        String[] doctorColumns = {"Name", "Specialty", "Phone"};
        adminDoctorsModel = new DefaultTableModel(doctorColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        adminDoctorsTable = new JTable(adminDoctorsModel);
        adminDoctorsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminDoctorsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminDoctorsTable.setRowHeight(25);
        adminDoctorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel searchPanel = createSearchPanel(adminDoctorsTable, adminDoctorsModel);
        JPanel topDoctorPanel = new JPanel(new BorderLayout());
        topDoctorPanel.add(addDoctorBtn, BorderLayout.WEST);
        topDoctorPanel.add(searchPanel, BorderLayout.EAST);

        doctorsTab.add(topDoctorPanel, BorderLayout.NORTH);
        doctorsTab.add(new JScrollPane(adminDoctorsTable), BorderLayout.CENTER);
        tabbedPane.add("Doctors", doctorsTab);

        JPanel billingTab = new JPanel(new BorderLayout());
        JButton createBillBtn = new JButton("Create Bill");
        applyNeumorphicButton(createBillBtn);
        createBillBtn.addActionListener(e -> createBillDialog());

        String[] billingColumns = {"Patient", "Services", "Total"};
        adminBillingModel = new DefaultTableModel(billingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        adminBillingTable = new JTable(adminBillingModel);
        adminBillingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminBillingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminBillingTable.setRowHeight(25);
        adminBillingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel searchBillingPanel = createSearchPanel(adminBillingTable, adminBillingModel);
        JPanel topBillingPanel = new JPanel(new BorderLayout());
        topBillingPanel.add(createBillBtn, BorderLayout.WEST);
        topBillingPanel.add(searchBillingPanel, BorderLayout.EAST);

        billingTab.add(topBillingPanel, BorderLayout.NORTH);
        billingTab.add(new JScrollPane(adminBillingTable), BorderLayout.CENTER);
        tabbedPane.add("Billing", billingTab);

        adminPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(adminPanel, "admin");
    }

    private JPanel createPatientsTab(boolean isAdmin) {
        JPanel patientsTab = new JPanel(new BorderLayout());

        String[] patientColumns = {"Name", "Age", "Gender", "Blood Type", "Phone", "Email"};
        DefaultTableModel model;
        JTable table;

        if (isAdmin) {
            adminPatientsModel = new DefaultTableModel(patientColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            adminPatientsTable = new JTable(adminPatientsModel);
            table = adminPatientsTable;
            model = adminPatientsModel;
        } else {
            doctorPatientsModel = new DefaultTableModel(patientColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            doctorPatientsTable = new JTable(doctorPatientsModel);
            table = doctorPatientsTable;
            model = doctorPatientsModel;
        }

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel searchPanel = createSearchPanel(table, model);

        if (!isAdmin) {
            JButton addPatientBtn = new JButton("Add New Patient");
            applyNeumorphicButton(addPatientBtn);
            addPatientBtn.addActionListener(e -> addPatientDialog());

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(addPatientBtn, BorderLayout.WEST);
            topPanel.add(searchPanel, BorderLayout.EAST);
            patientsTab.add(topPanel, BorderLayout.NORTH);
        } else {
            patientsTab.add(searchPanel, BorderLayout.NORTH);
        }

        patientsTab.add(new JScrollPane(table), BorderLayout.CENTER);
        return patientsTab;
    }

    private JPanel createAppointmentsTab(boolean isAdmin) {
        JPanel appointmentsTab = new JPanel(new BorderLayout());

        String[] appointmentColumns = {"Patient", "Doctor", "Type", "Date", "Time", "Notes"};
        DefaultTableModel model;
        JTable table;

        if (isAdmin) {
            adminAppointmentsModel = new DefaultTableModel(appointmentColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            adminAppointmentsTable = new JTable(adminAppointmentsModel);
            table = adminAppointmentsTable;
            model = adminAppointmentsModel;
        } else {
            doctorAppointmentsModel = new DefaultTableModel(appointmentColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            doctorAppointmentsTable = new JTable(doctorAppointmentsModel);
            table = doctorAppointmentsTable;
            model = doctorAppointmentsModel;
        }

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel searchPanel = createSearchPanel(table, model);

        if (!isAdmin) {
            JButton addAppointmentBtn = new JButton("Schedule Appointment");
            applyNeumorphicButton(addAppointmentBtn);
            addAppointmentBtn.addActionListener(e -> addAppointmentDialog());

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(addAppointmentBtn, BorderLayout.WEST);
            topPanel.add(searchPanel, BorderLayout.EAST);
            appointmentsTab.add(topPanel, BorderLayout.NORTH);
        } else {
            appointmentsTab.add(searchPanel, BorderLayout.NORTH);
        }

        appointmentsTab.add(new JScrollPane(table), BorderLayout.CENTER);
        return appointmentsTab;
    }

    private JPanel createSearchPanel(JTable table, DefaultTableModel model) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(235, 235, 235));

        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        applyNeumorphicButton(searchBtn);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        Runnable doSearch = () -> {
            String text = searchField.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        };

        searchBtn.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        return searchPanel;
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

    private void addDoctorDialog() {
        JDialog dialog = new JDialog(this, "Add Doctor", true);
        dialog.setSize(350, 250);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField();
        JTextField specialtyField = new JTextField();
        JTextField phoneField = new JTextField();

        dialog.add(new JLabel("Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Specialty:")); dialog.add(specialtyField);
        dialog.add(new JLabel("Phone:")); dialog.add(phoneField);

        JButton saveBtn = new JButton("Save");
        applyNeumorphicButton(saveBtn);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String specialty = specialtyField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || specialty.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidGhanaPhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Phone must be 10 digits starting with 02, 03, 04, or 05!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Doctor d = new Doctor(name, specialty, phone);
            doctors.add(d);
            db.saveDoctor(d); // SAVE TO DATABASE
            updateDoctorsTable();
            JOptionPane.showMessageDialog(dialog, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        dialog.add(new JLabel()); dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void updateDoctorsTable() {
        adminDoctorsModel.setRowCount(0);
        for (Doctor d : doctors) {
            adminDoctorsModel.addRow(new Object[]{d.name, d.specialty, d.phone});
        }
    }

    private void createBillDialog() {
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients available for billing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Create Bill", true);
        dialog.setSize(450, 400);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : patients) patientCombo.addItem(p.name);

        JList<String> servicesList = new JList<>();
        DefaultListModel<String> servicesModel = new DefaultListModel<>();
        for (Service s : services) {
            servicesModel.addElement(s.name + " - $" + s.price);
        }
        servicesList.setModel(servicesModel);
        servicesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        formPanel.add(new JLabel("Patient:"));
        formPanel.add(patientCombo);
        formPanel.add(new JLabel("Services (Select multiple):"));
        formPanel.add(new JScrollPane(servicesList));

        JButton saveBtn = new JButton("Generate Bill");
        applyNeumorphicButton(saveBtn);
        saveBtn.addActionListener(e -> {
            List<Integer> selectedIndices = servicesList.getSelectedValuesList()
                    .stream()
                    .map(servicesModel::indexOf)
                    .toList();

            if (selectedIndices.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select at least one service.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Service> selectedServices = new ArrayList<>();
            double total = 0;
            for (int idx : selectedIndices) {
                selectedServices.add(services.get(idx));
                total += services.get(idx).price;
            }

            Bill bill = new Bill((String) patientCombo.getSelectedItem(), selectedServices, total);
            bills.add(bill);
            db.saveBill(bill); // SAVE TO DATABASE
            updateBillingTable();
            JOptionPane.showMessageDialog(dialog, "Bill created! Total: $" + total, "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
        doctorPanel.setBackground(new Color(235, 235, 235));

        JLabel label = new JLabel("Doctor Dashboard");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton logoutBtn = new JButton("Logout");
        applyNeumorphicButton(logoutBtn);
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(235, 235, 235));
        topPanel.add(label, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        doctorPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Patients", createPatientsTab(false));
        tabbedPane.add("Appointments", createAppointmentsTab(false));

        doctorPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(doctorPanel, "doctor");
    }

    private boolean isValidGhanaPhone(String phone) {
        if (phone.length() != 10) return false;
        if (!phone.matches("\\d{10}")) return false;
        String prefix = phone.substring(0, 2);
        return prefix.equals("02") || prefix.equals("03") || prefix.equals("04") || prefix.equals("05");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidAge(String age) {
        try {
            int ageInt = Integer.parseInt(age);
            return ageInt >= 1 && ageInt <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String date) {
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) return false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date inputDate = sdf.parse(date);

            java.util.Date today = new java.util.Date();

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(today);
            cal2.setTime(inputDate);

            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);

            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);

            return !cal2.before(cal1);
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        if (!time.matches("\\d{2}:\\d{2}")) return false;

        String[] parts = time.split(":");
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDuplicatePatient(String name, String phone) {
        for (Patient p : patients) {
            if (p.name.equalsIgnoreCase(name) && p.phone.equals(phone)) {
                return true;
            }
        }
        return false;
    }

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
            db.savePatient(p); // SAVE TO DATABASE
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

    private void addAppointmentDialog() {
        if (doctors.isEmpty() || patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add patients and doctors first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Schedule Appointment", true);
        dialog.setSize(500, 450);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

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

        JButton saveBtn = new JButton("Save");
        applyNeumorphicButton(saveBtn);
        saveBtn.addActionListener(e -> {
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();

            if (date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Date and Time are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(dialog, "Date must be in DD/MM/YYYY format and not in the past!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidTime(time)) {
                JOptionPane.showMessageDialog(dialog, "Time must be in HH:MM format (24-hour)!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Appointment a = new Appointment(
                    (String) patientCombo.getSelectedItem(),
                    (String) doctorCombo.getSelectedItem(),
                    (String) typeCombo.getSelectedItem(),
                    date, time, notesField.getText()
            );
            appointments.add(a);
            db.saveAppointment(a); // SAVE TO DATABASE
            updateAppointmentsTable(doctorAppointmentsModel);
            if (adminAppointmentsModel != null) updateAppointmentsTable(adminAppointmentsModel);
            JOptionPane.showMessageDialog(dialog, "Appointment scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        dialog.add(new JLabel()); dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void updateAppointmentsTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        for (Appointment a : appointments) {
            model.addRow(new Object[]{a.patient, a.doctor, a.type, a.date, a.time, a.notes});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HMSNeumorphicAppModern::new);
    }

    // ------------------- Data Classes -------------------
    static class Patient {
        String name, age, gender, bloodType, phone, email, address, emergency, medicalHistory, allergies;
        public Patient(String name, String age, String gender, String bloodType,
                       String phone, String email, String address, String emergency,
                       String medicalHistory, String allergies) {
            this.name = name; this.age = age; this.gender = gender; this.bloodType = bloodType;
            this.phone = phone; this.email = email; this.address = address; this.emergency = emergency;
            this.medicalHistory = medicalHistory; this.allergies = allergies;
        }
        public String toString() {
            return "Name: "+name+"\nAge: "+age+"\nGender: "+gender+"\nBlood: "+bloodType+
                    "\nPhone: "+phone+"\nEmail: "+email+"\nAddress: "+address+
                    "\nEmergency: "+emergency+"\nHistory: "+medicalHistory+"\nAllergies: "+allergies;
        }
    }

    static class Doctor {
        String name, specialty, phone;
        public Doctor(String name, String specialty, String phone) {
            this.name = name; this.specialty = specialty; this.phone = phone;
        }
        public String toString() {
            return "Name: "+name+"\nSpecialty: "+specialty+"\nPhone: "+phone;
        }
    }

    static class Appointment {
        String patient, doctor, type, date, time, notes;
        public Appointment(String patient, String doctor, String type, String date, String time, String notes) {
            this.patient = patient; this.doctor = doctor; this.type = type;
            this.date = date; this.time = time; this.notes = notes;
        }
        public String toString() {
            return "Patient: "+patient+"\nDoctor: "+doctor+"\nType: "+type+"\nDate: "+date+"\nTime: "+time+"\nNotes: "+notes;
        }
    }

    static class Service {
        String name;
        double price;
        public Service(String name, double price) {
            this.name = name; this.price = price;
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
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Patient: ").append(patientName).append("\n");
            sb.append("Services:\n");
            for (Service s : services) {
                sb.append("  - ").append(s.name).append(": $").append(s.price).append("\n");
            }
            sb.append("Total Amount: $").append(total);
            return sb.toString();
        }
    }
}

// ------------------- DATABASE MANAGER CLASS -------------------
class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:hospital_data.db";
    private Connection conn;

    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("✓ Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("✗ Database connection error: " + e.getMessage());
        }
    }

    private void createTables() {
        String patientsTable = """
            CREATE TABLE IF NOT EXISTS patients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                age TEXT,
                gender TEXT,
                blood_type TEXT,
                phone TEXT,
                email TEXT,
                address TEXT,
                emergency_contact TEXT,
                medical_history TEXT,
                allergies TEXT
            )
        """;

        String doctorsTable = """
            CREATE TABLE IF NOT EXISTS doctors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                specialty TEXT,
                phone TEXT
            )
        """;

        String appointmentsTable = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_name TEXT NOT NULL,
                doctor_name TEXT NOT NULL,
                type TEXT,
                date TEXT,
                time TEXT,
                notes TEXT
            )
        """;

        String billsTable = """
            CREATE TABLE IF NOT EXISTS bills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_name TEXT NOT NULL,
                services TEXT,
                total REAL
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(patientsTable);
            stmt.execute(doctorsTable);
            stmt.execute(appointmentsTable);
            stmt.execute(billsTable);
            System.out.println("✓ Database tables created/verified!");
        } catch (SQLException e) {
            System.err.println("✗ Error creating tables: " + e.getMessage());
        }
    }

    public void savePatient(HMSNeumorphicAppModern.Patient patient) {
        String sql = "INSERT INTO patients (name, age, gender, blood_type, phone, email, address, emergency_contact, medical_history, allergies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.name);
            pstmt.setString(2, patient.age);
            pstmt.setString(3, patient.gender);
            pstmt.setString(4, patient.bloodType);
            pstmt.setString(5, patient.phone);
            pstmt.setString(6, patient.email);
            pstmt.setString(7, patient.address);
            pstmt.setString(8, patient.emergency);
            pstmt.setString(9, patient.medicalHistory);
            pstmt.setString(10, patient.allergies);
            pstmt.executeUpdate();
            System.out.println("✓ Patient saved to database!");
        } catch (SQLException e) {
            System.err.println("✗ Error saving patient: " + e.getMessage());
        }
    }

    public List<HMSNeumorphicAppModern.Patient> loadPatients() {
        List<HMSNeumorphicAppModern.Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HMSNeumorphicAppModern.Patient p = new HMSNeumorphicAppModern.Patient(
                    rs.getString("name"),
                    rs.getString("age"),
                    rs.getString("gender"),
                    rs.getString("blood_type"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("emergency_contact"),
                    rs.getString("medical_history"),
                    rs.getString("allergies")
                );
                patients.add(p);
            }
            System.out.println("✓ Loaded " + patients.size() + " patients");
        } catch (SQLException e) {
            System.err.println("✗ Error loading patients: " + e.getMessage());
        }
        
        return patients;
    }

    public void saveDoctor(HMSNeumorphicAppModern.Doctor doctor) {
        String sql = "INSERT INTO doctors (name, specialty, phone) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.name);
            pstmt.setString(2, doctor.specialty);
            pstmt.setString(3, doctor.phone);
            pstmt.executeUpdate();
            System.out.println("✓ Doctor saved to database!");
        } catch (SQLException e) {
            System.err.println("✗ Error saving doctor: " + e.getMessage());
        }
    }

    public List<HMSNeumorphicAppModern.Doctor> loadDoctors() {
        List<HMSNeumorphicAppModern.Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HMSNeumorphicAppModern.Doctor d = new HMSNeumorphicAppModern.Doctor(
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getString("phone")
                );
                doctors.add(d);
            }
            System.out.println("✓ Loaded " + doctors.size() + " doctors");
        } catch (SQLException e) {
            System.err.println("✗ Error loading doctors: " + e.getMessage());
        }
        
        return doctors;
    }

    public void saveAppointment(HMSNeumorphicAppModern.Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_name, doctor_name, type, date, time, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appointment.patient);
            pstmt.setString(2, appointment.doctor);
            pstmt.setString(3, appointment.type);
            pstmt.setString(4, appointment.date);
            pstmt.setString(5, appointment.time);
            pstmt.setString(6, appointment.notes);
            pstmt.executeUpdate();
            System.out.println("✓ Appointment saved to database!");
        } catch (SQLException e) {
            System.err.println("✗ Error saving appointment: " + e.getMessage());
        }
    }

    public List<HMSNeumorphicAppModern.Appointment> loadAppointments() {
        List<HMSNeumorphicAppModern.Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HMSNeumorphicAppModern.Appointment a = new HMSNeumorphicAppModern.Appointment(
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("type"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("notes")
                );
                appointments.add(a);
            }
            System.out.println("✓ Loaded " + appointments.size() + " appointments");
        } catch (SQLException e) {
            System.err.println("✗ Error loading appointments: " + e.getMessage());
        }
        
        return appointments;
    }

    public void saveBill(HMSNeumorphicAppModern.Bill bill) {
        String sql = "INSERT INTO bills (patient_name, services, total) VALUES (?, ?, ?)";
        
        StringBuilder servicesStr = new StringBuilder();
        for (int i = 0; i < bill.services.size(); i++) {
            servicesStr.append(bill.services.get(i).name).append(":").append(bill.services.get(i).price);
            if (i < bill.services.size() - 1) servicesStr.append(",");
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.patientName);
            pstmt.setString(2, servicesStr.toString());
            pstmt.setDouble(3, bill.total);
            pstmt.executeUpdate();
            System.out.println("✓ Bill saved to database!");
        } catch (SQLException e) {
            System.err.println("✗ Error saving bill: " + e.getMessage());
        }
    }

    public List<HMSNeumorphicAppModern.Bill> loadBills() {
        List<HMSNeumorphicAppModern.Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String servicesStr = rs.getString("services");
                List<HMSNeumorphicAppModern.Service> servicesList = new ArrayList<>();
                
                if (servicesStr != null && !servicesStr.isEmpty()) {
                    String[] serviceParts = servicesStr.split(",");
                    for (String part : serviceParts) {
                        String[] namePrice = part.split(":");
                        if (namePrice.length == 2) {
                            servicesList.add(new HMSNeumorphicAppModern.Service(
                                namePrice[0], 
                                Double.parseDouble(namePrice[1])
                            ));
                        }
                    }
                }
                
                HMSNeumorphicAppModern.Bill b = new HMSNeumorphicAppModern.Bill(
                    rs.getString("patient_name"),
                    servicesList,
                    rs.getDouble("total")
                );
                bills.add(b);
            }
            System.out.println("✓ Loaded " + bills.size() + " bills");
        } catch (SQLException e) {
            System.err.println("✗ Error loading bills: " + e.getMessage());
        }
        
        return bills;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }
}

