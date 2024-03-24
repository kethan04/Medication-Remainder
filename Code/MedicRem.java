import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.border.Border;
import java.util.List;
import java.util.Timer;

public class MedicRem {
    private JFrame frame;
    private JTextField medicationNameField;
    private JTextField dosageField;
    private JTextField frequencyField;
    private JTextField dateTimeField;
    private String loggedInUser;

    private List<MedicationReminder> reminders;

    public MedicRem(String loggedInUser) {
        this.loggedInUser = loggedInUser;
        frame = new JFrame("Medication MedicRem");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        reminders = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));
        panel.setBackground(new Color(173, 216, 230));

        Font robotoFont = new Font("Roboto", Font.BOLD, 16);

        JLabel medicationNameLabel = new JLabel("Medication Name:");
        medicationNameLabel.setFont(robotoFont);
        medicationNameLabel.setForeground(Color.RED);
        medicationNameField = new JTextField();
        medicationNameField.setFont(robotoFont);
        panel.add(medicationNameLabel);
        panel.add(medicationNameField);

        JLabel dosageLabel = new JLabel("Dosage:");
        dosageLabel.setFont(robotoFont);
        dosageLabel.setForeground(Color.RED);
        dosageField = new JTextField();
        dosageField.setFont(robotoFont);
        panel.add(dosageLabel);
        panel.add(dosageField);

        JLabel frequencyLabel = new JLabel("Frequency:");
        frequencyLabel.setFont(robotoFont);
        frequencyLabel.setForeground(Color.RED);
        frequencyField = new JTextField();
        frequencyField.setFont(robotoFont);
        panel.add(frequencyLabel);
        panel.add(frequencyField);

        JLabel dateTimeLabel = new JLabel("Scheduled Date and Time (MM/dd/yyyy hh:mm a):");
        dateTimeLabel.setFont(robotoFont);
        dateTimeLabel.setForeground(Color.RED);
        dateTimeField = new JTextField();
        dateTimeField.setFont(robotoFont);
        panel.add(dateTimeLabel);
        panel.add(dateTimeField);

        JButton addReminderButton = new JButton("Add Reminder");
        addReminderButton.setFont(robotoFont);
        addReminderButton.setBackground(new Color(255, 69, 0));
        addReminderButton.setForeground(Color.WHITE);
        panel.add(addReminderButton);

        JButton activeRemindersButton = new JButton("Active Reminders");
        activeRemindersButton.setFont(new Font("Roboto", Font.BOLD, 14));
        activeRemindersButton.setBackground(new Color(0, 128, 0));
        activeRemindersButton.setForeground(Color.WHITE);
        panel.add(activeRemindersButton);

        int borderRadius = 15;
        addReminderButton.setBorder(new RoundedBorder(borderRadius));
        activeRemindersButton.setBorder(new RoundedBorder(borderRadius));

        addReminderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitMedicRem();
            }
        });

        activeRemindersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showActiveReminders();
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private void submitMedicRem() {
        String medicationName = medicationNameField.getText();
        String dosage = dosageField.getText();
        String frequency = frequencyField.getText();
        String inputDateTime = dateTimeField.getText();

        String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE";
        String username = "system";
        String password = "1098";

        Connection connection = null;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            String insertSQL = "INSERT INTO MedicationReminder (Username, MedicationName, Dosage, Frequency, ScheduledDateTime) " +
                    "VALUES (?, ?, ?, ?, TO_TIMESTAMP(?, 'MM/DD/YYYY HH24:MI'))";

            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, loggedInUser);
            preparedStatement.setString(2, medicationName);
            preparedStatement.setString(3, dosage);
            preparedStatement.setString(4, frequency);
            String formattedDateTime = convertTo24HourFormat(inputDateTime);
            preparedStatement.setString(5, formattedDateTime);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                MedicationReminder reminder = new MedicationReminder(formattedDateTime, medicationName, dosage, frequency);
                scheduleReminder(reminder);
                reminders.add(reminder);

                showCustomMessageBox("Medication Reminder Added", "Medicine Name: " + medicationName + "\nDosage: " + dosage + "\nFrequency: " + frequency + "\nTime: " + formattedDateTime, "success");
            } else {
                showCustomMessageBox("Failed to Add Reminder", "Please check your input.", "error");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            showCustomMessageBox("Database Error", "Error: " + ex.getMessage(), "error");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showActiveReminders() {
        StringBuilder reminderText = new StringBuilder("Active Reminders:\n\n");
        for (MedicationReminder reminder : reminders) {
            reminderText.append("Medication Name: ").append(reminder.getMedicationName()).append("\n");
            reminderText.append("Dosage: ").append(reminder.getDosage()).append("\n");
            reminderText.append("Frequency: ").append(reminder.getFrequency()).append("\n");
            reminderText.append("Scheduled Time: ").append(reminder.getScheduledDateTime()).append("\n\n");
        }

        if (reminders.isEmpty()) {
            reminderText.append("No active reminders.");
        }

        showCustomMessageBox("Active Reminders", reminderText.toString(), "info");
    }

    private void scheduleReminder(MedicationReminder reminder) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date scheduledDateTime = sdf.parse(reminder.getScheduledDateTime());

            Date currentTime = new Date();
            if (scheduledDateTime.before(currentTime)) {
                showCustomMessageBox("The time for your medication has already passed. You cannot set a past time.", "Reminder Alert", "error");
                reminders.remove(reminder);
            } else {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        showReminder(reminder.getMedicationName(), reminder.getDosage(), reminder.getFrequency());
                        reminders.remove(reminder);
                    }
                }, scheduledDateTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            showCustomMessageBox("Invalid date and time format. Please use MM/dd/yyyy hh:mm AM/PM.", "Error", "error");
        }
    }

    private void showReminder(String medicationName, String dosage, String frequency) {
        showCustomMessageBox("Take Your Medicine",
                "Time to take your medication:\n" +
                        "Medication Name: " + medicationName + "\n" +
                        "Dosage: " + dosage + "\n" +
                        "Frequency: " + frequency,
                "info");
    }

    private String convertTo24HourFormat(String inputDateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            Date date = inputFormat.parse(inputDateTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            return outputFormat.format(calendar.getTime());
        } catch (ParseException e) {
            showCustomMessageBox("Invalid Date and Time Format", "Please use MM/dd/yyyy hh:mm AM/PM.", "error");
            return "";
        }
    }

    private void showCustomMessageBox(String title, String message, String messageType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 18));
        JLabel titleLabel2 = new JLabel("");

        JTextArea textArea = new JTextArea(message);
        textArea.setFont(new Font("Roboto", Font.PLAIN, 14));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);

        panel.add(titleLabel);
        panel.add(titleLabel2);
        panel.add(textArea);

        if (messageType.equals("success")) {
            titleLabel.setForeground(Color.GREEN);
        } else if (messageType.equals("error")) {
            titleLabel.setForeground(Color.RED);
        } else if (messageType.equals("info")) {
            titleLabel.setForeground(new Color(0, 102, 204));
        }

        ImageIcon alertIcon = new ImageIcon("alert_icon.png");
        int optionType = JOptionPane.DEFAULT_OPTION;
        int messageTypeCode = JOptionPane.PLAIN_MESSAGE;
        JOptionPane.showOptionDialog(null, panel, "Message", optionType, messageTypeCode, alertIcon, new Object[]{"OK"}, null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MedicRem("Your_Logged_In_Username"));
    }
}

class MedicationReminder {
    private String scheduledDateTime;
    private String medicationName;
    private String dosage;
    private String frequency;

    public MedicationReminder(String scheduledDateTime, String medicationName, String dosage, String frequency) {
        this.scheduledDateTime = scheduledDateTime;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
    }

    public String getScheduledDateTime() {
        return scheduledDateTime;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }
}

class RoundedBorder implements Border {
    private int radius;

    public RoundedBorder(int radius) {
        this.radius = radius;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius + 1);
    }

    public boolean isBorderOpaque() {
        return true;
    }
}
