import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class UserRegistrationLogin2 {
    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public UserRegistrationLogin2() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));
        panel.setBackground(new Color(173, 216, 230));

        Border roundedBorder = new EmptyBorder(10, 10, 10, 10);

        Font robotoFont = new Font("Roboto", Font.BOLD, 16);

        JLabel welcomeLabel = new JLabel("Welcome to Medical Reminder ");
        welcomeLabel.setFont(new Font("Roboto", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.BLACK);
        panel.add(welcomeLabel);
        panel.add(new JLabel());

        JLabel subtitleLabel = new JLabel("An Application for Reminding You to Take Your Pills On Time");
        subtitleLabel.setFont(new Font("Roboto", Font.BOLD, 14));
        subtitleLabel.setForeground(Color.BLACK);
        panel.add(subtitleLabel);
        panel.add(new JLabel());

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(robotoFont);
        usernameLabel.setForeground(Color.BLACK); 
        panel.add(usernameLabel);
        usernameField = new JTextField();
        usernameField.setFont(robotoFont);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(robotoFont);
        passwordLabel.setForeground(Color.BLACK); 
        panel.add(passwordLabel);
        passwordField = new JPasswordField();
        passwordField.setFont(robotoFont);
        panel.add(passwordField);

        
        JButton registerButton = new JButton("Register");
        registerButton.setFont(robotoFont);
        registerButton.setBackground(new Color(255, 0, 0));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(roundedBorder);
        panel.add(registerButton);

        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(robotoFont);
        loginButton.setBackground(new Color(0, 128, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(roundedBorder);
        panel.add(loginButton);

        
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Oracle JDBC driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (registerUser(username, password)) {
                    JOptionPane.showMessageDialog(null, "Registration successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Registration failed. Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (loginUser(username, password)) {
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                    topFrame.dispose();

                   
                    new MedicRem(username); 
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed. Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private static boolean registerUser(String username, String password) {
        
        String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        String dbUsername = "system";
        String dbPassword = "1098";


        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
            String insertSQL = "INSERT INTO Users (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            int rowsAffected = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static boolean loginUser(String username, String password) {
        
        String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        String dbUsername = "system";
        String dbPassword = "1098";

        
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
            String selectSQL = "SELECT username, password FROM Users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            boolean loginSuccessful = resultSet.next();

            resultSet.close();
            preparedStatement.close();
            connection.close();

            return loginSuccessful;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserRegistrationLogin2 login = new UserRegistrationLogin2();
            JFrame frame = new JFrame("Medical Reminder Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 300);
            frame.setLocationRelativeTo(null);
            frame.add(login.getPanel());
            frame.setVisible(true);
        });
    }
}
