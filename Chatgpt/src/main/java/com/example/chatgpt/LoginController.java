package com.example.chatgpt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    // In your login controller
    public void handleLogin() {
        // After successful login
        String username = usernameField.getText(); // Get username from the input field
        FXMLLoader loader = null;
        SubjectsController subjectsController = loader.getController();
        subjectsController.setLoggedInUser(username);
        // Load the Subjects.fxml scene
    }


    public static String loggedInUsername;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private Connection connectDB() {
        String url = "jdbc:mysql://localhost:3306/userdb";
        String user = "root";
        String password = "Kesh9136@";  // Replace with your MySQL password

        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to the database: " + e.getMessage());
        }
        return conn;
    }

    public void testDatabaseConnection() {
        try (Connection conn = connectDB()) {
            if (conn != null) {
                System.out.println("Database connection test successful!");
            } else {
                System.out.println("Database connection test failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all fields.");
            return;
        }

        // Query to authenticate user
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                loggedInUsername = username; // Store the logged-in user for future use
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful.");

                // Load the Dashboard FXML and open it
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
                Parent root = fxmlLoader.load();

                // Pass the logged-in user to the dashboard controller
                DashboardController dashboardController = fxmlLoader.getController();
                dashboardController.setLoggedInUsername(loggedInUsername);


                // Get the current stage and set the new scene for the dashboard
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Dashboard");
                stage.show();


            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error during login: " + e.getMessage());
        }
    }

//    @FXML
//    private void openSignUpPage() {
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
//            Parent root = fxmlLoader.load();
//            Stage stage = new Stage();
//            stage.setTitle("Sign Up");
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @FXML
    private void openSignPage() {
        try {
            // Load the Login FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
            Parent root = fxmlLoader.load();

            // Get the current stage (window) and set the new scene to the login page
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SignUp Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void openLoginPage() {
        try {
            // Load the Login FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new stage for the login page
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
