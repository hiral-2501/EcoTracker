import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class EcoTrackerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}

class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);
    private Map<String, User> users = new HashMap<>();
    private User currentUser;
    private static final String USERS_FILE = "users.dat";

    public MainFrame() {
        setTitle("EcoTracker - Carbon Footprint Tracker");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadUsers();

        cardPanel.add(new LoginPanel(this), "LOGIN");
        cardPanel.add(new RegistrationPanel(this), "REGISTER");
        // Don't add DashboardPanel here - we'll add it when needed

        add(cardPanel);
        showLogin();
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                users = (Map<String, User>) obj;
            }
        } catch (Exception e) {
            users = new HashMap<>();
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showLogin() {
        cardLayout.show(cardPanel, "LOGIN");
    }

    public void showRegistration() {
        cardLayout.show(cardPanel, "REGISTER");
    }

    public void showDashboard(User user) {
        this.currentUser = user;
        
        // Remove existing dashboard if it exists
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof DashboardPanel) {
                cardPanel.remove(c);
                break;
            }
        }
        
        // Add new dashboard
        DashboardPanel dashboard = new DashboardPanel(this);
        cardPanel.add(dashboard, "DASHBOARD");
        dashboard.refreshData();
        cardLayout.show(cardPanel, "DASHBOARD");
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password));
        saveUsers();
        return true;
    }

    public User login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}



class User implements Serializable {
    private String username;
    private String password;
    private List<EmissionRecord> emissionHistory = new ArrayList<>();
    private double monthlyBudget = 100; // Default 100kg CO2

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public List<EmissionRecord> getEmissionHistory() { return emissionHistory; }
    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double budget) { this.monthlyBudget = budget; }

    public void addEmissionRecord(EmissionRecord record) {
        emissionHistory.add(record);
    }

    public double getMonthlyTotal() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);
        
        return emissionHistory.stream()
            .filter(record -> {
                String[] parts = record.getDate().split("-");
                int recordMonth = Integer.parseInt(parts[1]) - 1;
                int recordYear = Integer.parseInt(parts[0]);
                return recordMonth == currentMonth && recordYear == currentYear;
            })
            .mapToDouble(EmissionRecord::getTotalEmissions)
            .sum();
    }

    public double getTodayTotal() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        return emissionHistory.stream()
            .filter(record -> record.getDate().equals(today))
            .mapToDouble(EmissionRecord::getTotalEmissions)
            .sum();
    }

    static class EmissionRecord implements Serializable {
        private String date;
        private double transportationEmissions;
        private double electricityEmissions;
        private double foodEmissions;
        private double totalEmissions;

        public EmissionRecord(String date, double transportationEmissions, 
                            double electricityEmissions, double foodEmissions) {
            this.date = date;
            this.transportationEmissions = transportationEmissions;
            this.electricityEmissions = electricityEmissions;
            this.foodEmissions = foodEmissions;
            this.totalEmissions = transportationEmissions + electricityEmissions + foodEmissions;
        }

        public String getDate() { return date; }
        public double getTransportationEmissions() { return transportationEmissions; }
        public double getElectricityEmissions() { return electricityEmissions; }
        public double getFoodEmissions() { return foodEmissions; }
        public double getTotalEmissions() { return totalEmissions; }
    }
}

class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("EcoTracker Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::performLogin);
        add(loginButton, gbc);

        gbc.gridx = 1;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> mainFrame.showRegistration());
        add(registerButton, gbc);
    }

    private void performLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = mainFrame.login(username, password);
        if (user != null) {
            mainFrame.showDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class RegistrationPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Register New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(this::performRegistration);
        add(registerButton, gbc);

        gbc.gridx = 1;
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> mainFrame.showLogin());
        add(backButton, gbc);
    }

    private void performRegistration(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mainFrame.register(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private JLabel todayEmissionLabel;
    private JLabel monthlyTotalLabel;
    private JLabel budgetLabel;
    private JProgressBar budgetProgressBar;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Welcome, ");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainFrame.showLogin());
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Add Entry", createEntryPanel());
        tabbedPane.addTab("History", createHistoryPanel());
        tabbedPane.addTab("Charts", createChartsPanel());
        tabbedPane.addTab("Budget", createBudgetPanel());
        tabbedPane.addTab("Suggestions", createSuggestionsPanel());
        tabbedPane.addTab("Help", createHelpPanel());
        tabbedPane.addTab("Feedback", createFeedbackPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1));
        todayEmissionLabel = new JLabel("Today's Emission: 0 kg CO₂");
        monthlyTotalLabel = new JLabel("Monthly Total: 0 kg / 100 kg (0%)");
        budgetLabel = new JLabel("Budget Status: Good");
        
        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        
        summaryPanel.add(welcomeLabel);
        summaryPanel.add(todayEmissionLabel);
        summaryPanel.add(monthlyTotalLabel);
        summaryPanel.add(budgetLabel);
        summaryPanel.add(budgetProgressBar);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        // Chart Panel
        JPanel chartPanel = new JPanel();
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createEntryPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Date
        panel.add(new JLabel("Date:"));
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        panel.add(dateField);
        
        // Category
        panel.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Transport", "Electricity", "Food"});
        panel.add(categoryCombo);
        
        // Transport Fields
        JPanel transportPanel = new JPanel(new GridLayout(0, 2));
        transportPanel.add(new JLabel("Transport Type:"));
        JComboBox<String> transportTypeCombo = new JComboBox<>(new String[]{"Car", "Bus", "Train"});
        transportPanel.add(transportTypeCombo);
        
        transportPanel.add(new JLabel("Distance (km):"));
        JTextField distanceField = new JTextField();
        transportPanel.add(distanceField);
        
        // Electricity Fields
        JPanel electricityPanel = new JPanel(new GridLayout(0, 2));
        electricityPanel.add(new JLabel("Electricity Usage (kWh):"));
        JTextField electricityField = new JTextField();
        electricityPanel.add(electricityField);
        
        // Food Fields
        JPanel foodPanel = new JPanel(new GridLayout(0, 2));
        foodPanel.add(new JLabel("Food Type:"));
        JComboBox<String> foodTypeCombo = new JComboBox<>(new String[]{"Beef", "Chicken", "Pork", "Fish", "Vegetarian", "Vegan"});
        foodPanel.add(foodTypeCombo);
        
        foodPanel.add(new JLabel("Servings:"));
        JTextField servingsField = new JTextField();
        foodPanel.add(servingsField);
        
        // Card Layout for dynamic fields
        JPanel dynamicFieldsPanel = new JPanel(new CardLayout());
        dynamicFieldsPanel.add(transportPanel, "Transport");
        dynamicFieldsPanel.add(electricityPanel, "Electricity");
        dynamicFieldsPanel.add(foodPanel, "Food");
        
        categoryCombo.addActionListener(e -> {
            CardLayout cl = (CardLayout) dynamicFieldsPanel.getLayout();
            cl.show(dynamicFieldsPanel, (String) categoryCombo.getSelectedItem());
        });
        
        panel.add(dynamicFieldsPanel);
        panel.add(new JLabel()); // placeholder
        
        // Add Button
        JButton addButton = new JButton("Add Activity");
        addButton.addActionListener(e -> {
            String date = dateField.getText();
            String category = (String) categoryCombo.getSelectedItem();
            
            double transportEmissions = 0;
            double electricityEmissions = 0;
            double foodEmissions = 0;
            
            switch (category) {
                case "Transport":
                    String transportType = (String) transportTypeCombo.getSelectedItem();
                    double distance = Double.parseDouble(distanceField.getText());
                    transportEmissions = CarbonCalculator.calculateTransportEmissions(transportType, distance);
                    break;
                case "Electricity":
                    double usage = Double.parseDouble(electricityField.getText());
                    electricityEmissions = CarbonCalculator.calculateElectricityEmissions(usage);
                    break;
                case "Food":
                    String foodType = (String) foodTypeCombo.getSelectedItem();
                    int servings = Integer.parseInt(servingsField.getText());
                    foodEmissions = CarbonCalculator.calculateFoodEmissions(foodType, servings);
                    break;
            }
            
            User.EmissionRecord record = new User.EmissionRecord(
                date, transportEmissions, electricityEmissions, foodEmissions);
            
            mainFrame.getCurrentUser().addEmissionRecord(record);
            JOptionPane.showMessageDialog(this, "Activity added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        });
        
        panel.add(addButton);
        
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JTable historyTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(historyTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton exportButton = new JButton("Export to PDF");
        exportButton.addActionListener(e -> {
            PDFGenerator.generateReport(mainFrame.getCurrentUser());
            JOptionPane.showMessageDialog(this, "Report generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        panel.add(exportButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        
        JButton barChartButton = new JButton("Show Bar Chart");
        barChartButton.addActionListener(e -> {
            ChartGenerator.showBarChart(mainFrame.getCurrentUser().getEmissionHistory());
        });
        
        JButton pieChartButton = new JButton("Show Pie Chart");
        pieChartButton.addActionListener(e -> {
            ChartGenerator.showPieChart(mainFrame.getCurrentUser().getEmissionHistory());
        });
        
        panel.add(barChartButton);
        panel.add(pieChartButton);
        
        return panel;
    }

    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Current Monthly Budget:"));
        JLabel currentBudgetLabel = new JLabel(String.valueOf(mainFrame.getCurrentUser().getMonthlyBudget()));
        inputPanel.add(currentBudgetLabel);
        
        inputPanel.add(new JLabel("New Budget:"));
        JTextField newBudgetField = new JTextField();
        inputPanel.add(newBudgetField);
        
        JButton updateButton = new JButton("Update Budget");
        updateButton.addActionListener(e -> {
            try {
                double newBudget = Double.parseDouble(newBudgetField.getText());
                mainFrame.getCurrentUser().setMonthlyBudget(newBudget);
                currentBudgetLabel.setText(String.valueOf(newBudget));
                refreshData();
                JOptionPane.showMessageDialog(this, "Budget updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(updateButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createSuggestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea suggestionsArea = new JTextArea();
        suggestionsArea.setEditable(false);
        
        JButton generateButton = new JButton("Generate Suggestions");
        generateButton.addActionListener(e -> {
            List<String> suggestions = SuggestionEngine.generateSuggestions(mainFrame.getCurrentUser());
            StringBuilder sb = new StringBuilder();
            for (String suggestion : suggestions) {
                sb.append("• ").append(suggestion).append("\n");
            }
            suggestionsArea.setText(sb.toString());
        });
        
        panel.add(new JScrollPane(suggestionsArea), BorderLayout.CENTER);
        panel.add(generateButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JComboBox<String> topicCombo = new JComboBox<>(new String[]{
            "Getting Started", "Add Activity", "Budget", "Reports", "Feedback"
        });
        
        JTextArea helpTextArea = new JTextArea();
        helpTextArea.setEditable(false);
        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        
        topicCombo.addActionListener(e -> {
            String topic = (String) topicCombo.getSelectedItem();
            switch (topic) {
                case "Getting Started":
                    helpTextArea.setText("Welcome to EcoTracker!\n\nThis app helps you track your carbon footprint. "
                            + "Start by adding your daily activities in the 'Add Entry' tab.");
                    break;
                case "Add Activity":
                    helpTextArea.setText("To add an activity:\n1. Select the category (Transport, Electricity, Food)\n"
                            + "2. Fill in the details for the selected category\n3. Click 'Add Activity'");
                    break;
                case "Budget":
                    helpTextArea.setText("Set your monthly carbon budget in the 'Budget' tab. "
                            + "The app will track your progress and alert you when you're close to your limit.");
                    break;
                case "Reports":
                    helpTextArea.setText("View your emission history and charts in the 'History' and 'Charts' tabs. "
                            + "You can export reports to PDF for sharing or record keeping.");
                    break;
                case "Feedback":
                    helpTextArea.setText("Have suggestions or found a bug? Use the 'Feedback' tab to let us know!");
                    break;
            }
        });
        
        panel.add(topicCombo, BorderLayout.NORTH);
        panel.add(new JScrollPane(helpTextArea), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(mainFrame.getCurrentUser().getUsername());
        usernameField.setEditable(false);
        panel.add(usernameField);
        
        panel.add(new JLabel("Rating:"));
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"★☆☆☆☆", "★★☆☆☆", "★★★☆☆", "★★★★☆", "★★★★★"});
        panel.add(ratingCombo);
        
        panel.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Bug", "Suggestion", "UI"});
        panel.add(categoryCombo);
        
        panel.add(new JLabel("Comments:"));
        JTextArea commentsArea = new JTextArea(5, 20);
        panel.add(new JScrollPane(commentsArea));
        
        JButton submitButton = new JButton("Submit Feedback");
        submitButton.addActionListener(e -> {
            String rating = (String) ratingCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String comments = commentsArea.getText();
            
            // In a real app, you would save this feedback
            JOptionPane.showMessageDialog(this, "Thank you for your feedback!", "Success", JOptionPane.INFORMATION_MESSAGE);
            commentsArea.setText("");
        });
        
        panel.add(submitButton);
        
        return panel;
    }

    public void refreshData() {
        User user = mainFrame.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        todayEmissionLabel.setText(String.format("Today's Emission: %.1f kg CO₂", user.getTodayTotal()));
        
        double monthlyTotal = user.getMonthlyTotal();
        double budget = user.getMonthlyBudget();
        double percentage = (monthlyTotal / budget) * 100;
        
        monthlyTotalLabel.setText(String.format("Monthly Total: %.1f kg / %.1f kg (%.1f%%)", 
            monthlyTotal, budget, percentage));
        
        budgetLabel.setText(String.format("Budget Status: %s", 
            percentage > 90 ? "Warning!" : percentage > 70 ? "Close to limit" : "Good"));
        
        budgetProgressBar.setValue((int) percentage);
        budgetProgressBar.setForeground(percentage > 90 ? Color.RED : percentage > 70 ? Color.ORANGE : Color.GREEN);
    }
}

class CarbonCalculator {
    // Emission factors (kg CO2 per unit)
    private static final double CAR_EMISSION = 0.2; // per km
    private static final double BUS_EMISSION = 0.1; // per km
    private static final double TRAIN_EMISSION = 0.05; // per km
    private static final double ELECTRICITY_EMISSION = 0.5; // per kWh
    private static final double BEEF_EMISSION = 2.5; // per serving
    private static final double CHICKEN_EMISSION = 0.6; // per serving
    private static final double PORK_EMISSION = 0.7; // per serving
    private static final double FISH_EMISSION = 0.4; // per serving
    private static final double VEGETARIAN_EMISSION = 0.3; // per serving
    private static final double VEGAN_EMISSION = 0.2; // per serving

    public static double calculateTransportEmissions(String transportType, double distance) {
        switch (transportType) {
            case "Car": return distance * CAR_EMISSION;
            case "Bus": return distance * BUS_EMISSION;
            case "Train": return distance * TRAIN_EMISSION;
            default: return 0;
        }
    }

    public static double calculateElectricityEmissions(double usage) {
        return usage * ELECTRICITY_EMISSION;
    }

    public static double calculateFoodEmissions(String foodType, int servings) {
        switch (foodType) {
            case "Beef": return servings * BEEF_EMISSION;
            case "Chicken": return servings * CHICKEN_EMISSION;
            case "Pork": return servings * PORK_EMISSION;
            case "Fish": return servings * FISH_EMISSION;
            case "Vegetarian": return servings * VEGETARIAN_EMISSION;
            case "Vegan": return servings * VEGAN_EMISSION;
            default: return 0;
        }
    }
}

class ChartGenerator {
    public static void showBarChart(List<User.EmissionRecord> records) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (User.EmissionRecord record : records) {
            dataset.addValue(record.getTransportationEmissions(), "Transport", record.getDate());
            dataset.addValue(record.getElectricityEmissions(), "Electricity", record.getDate());
            dataset.addValue(record.getFoodEmissions(), "Food", record.getDate());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Carbon Emissions by Category", 
            "Date", 
            "CO2 Emissions (kg)", 
            dataset
        );
        
        displayChart(chart);
    }

    public static void showPieChart(List<User.EmissionRecord> records) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        double transportTotal = records.stream().mapToDouble(User.EmissionRecord::getTransportationEmissions).sum();
        double electricityTotal = records.stream().mapToDouble(User.EmissionRecord::getElectricityEmissions).sum();
        double foodTotal = records.stream().mapToDouble(User.EmissionRecord::getFoodEmissions).sum();
        
        dataset.setValue("Transport", transportTotal);
        dataset.setValue("Electricity", electricityTotal);
        dataset.setValue("Food", foodTotal);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Carbon Emissions Breakdown", 
            dataset, 
            true, true, false
        );
        
        displayChart(chart);
    }

    private static void displayChart(JFreeChart chart) {
        JFrame chartFrame = new JFrame("EcoTracker Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(800, 600);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        chartFrame.setContentPane(chartPanel);
        
        chartFrame.pack();
        chartFrame.setVisible(true);
    }
}


class PDFGenerator {
    public static void generateReport(User user) {
        Document document = new Document();
        try {
            String filename = "EcoTracker_Report_" + user.getUsername() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            
            document.add(new Paragraph("EcoTracker Emission Report"));
            document.add(new Paragraph("User: " + user.getUsername()));
            document.add(new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            document.add(new Paragraph("\n"));
            
            // Monthly Summary
            double monthlyTotal = user.getMonthlyTotal();
            double budget = user.getMonthlyBudget();
            document.add(new Paragraph(String.format("Monthly Total: %.1f kg CO2", monthlyTotal)));
            document.add(new Paragraph(String.format("Monthly Budget: %.1f kg CO2", budget)));
            document.add(new Paragraph(String.format("Remaining Budget: %.1f kg CO2", budget - monthlyTotal)));
            document.add(new Paragraph("\n"));
            
            // Detailed Records
            document.add(new Paragraph("Emission Records:"));
            for (User.EmissionRecord record : user.getEmissionHistory()) {
                document.add(new Paragraph(String.format(
                    "Date: %s - Transport: %.1f kg, Electricity: %.1f kg, Food: %.1f kg, Total: %.1f kg",
                    record.getDate(), record.getTransportationEmissions(),
                    record.getElectricityEmissions(), record.getFoodEmissions(),
                    record.getTotalEmissions()
                )));
            }
            
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SuggestionEngine {
    public static List<String> generateSuggestions(User user) {
        List<String> suggestions = new ArrayList<>();
        List<User.EmissionRecord> records = user.getEmissionHistory();
        
        double transportTotal = records.stream().mapToDouble(User.EmissionRecord::getTransportationEmissions).sum();
        double electricityTotal = records.stream().mapToDouble(User.EmissionRecord::getElectricityEmissions).sum();
        double foodTotal = records.stream().mapToDouble(User.EmissionRecord::getFoodEmissions).sum();
        
        // Transport suggestions
        if (transportTotal > 20) {
            suggestions.add("Consider using public transport or carpooling to reduce your transport emissions.");
        }
        
        // Electricity suggestions
        if (electricityTotal > 15) {
            suggestions.add("Turn off lights and appliances when not in use. Consider energy-efficient devices.");
        }
        
        // Food suggestions
        if (foodTotal > 10) {
            suggestions.add("Reducing meat consumption, especially beef, can significantly lower your food emissions.");
        }
        
        // Budget suggestions
        double percentage = (user.getMonthlyTotal() / user.getMonthlyBudget()) * 100;
        if (percentage > 90) {
            suggestions.add("You're close to your monthly budget! Consider reducing high-emission activities.");
        } else if (percentage > 70) {
            suggestions.add("You've used over 70% of your budget. Plan your remaining activities carefully.");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Great job! Your emissions are well managed. Keep up the good work!");
        }
        
        return suggestions;
    }
}