package com.example.chatgpt;

import com.example.chatgpt.DatabaseConnection;
import com.example.chatgpt.LoginController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class TaskManagerController {

    private NotificationManager notificationManager;

    @FXML
    private Button taskManagerButton;
    // Method to show an alert for the Task Manager section
    @FXML
    private void showTaskManagerAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Task Manager");
        alert.setHeaderText(null);
        alert.setContentText("This is the Task Manager section.");
        alert.showAndWait();
    }

    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    @FXML
    private void logOut() {
        notificationManager.shutdown();
        // Close the current window (dashboard)
        Stage stage = (Stage) Stage.getWindows().get(0);
        stage.close();

        // Redirect to login page after logging out
        LoginController loginController = new LoginController();
        loginController.openLoginPage();
    }

    @FXML
    private Button showTasksButton;  // Declare the button


    @FXML
    private void showTasks() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ShowTasks.fxml"));
            Parent root = fxmlLoader.load();

            // Get the controller for ShowTasks and pass the logged-in user
            ShowTasksController showTasksController = fxmlLoader.getController();
            showTasksController.setLoggedInUser(LoginController.loggedInUsername);  // Pass logged-in user

            // Show the new page in the current window
            Stage stage = (Stage) showTasksButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Tasks");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private Button homeButton;

    @FXML
    private void goHome() {
        // Navigate to the Dashboard page
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent root = fxmlLoader.load();

            DashboardController dashboardController = fxmlLoader.getController();
            dashboardController.setLoggedInUsername(loggedInUsername); // Pass the logged-in user

            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button performanceButton; // Declare the performance button

    @FXML
    private void goToPerformance() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("subjects.fxml")); // Load the subjects FXML
            Parent root = fxmlLoader.load();

            // Get the controller for Subjects and pass the logged-in user
            SubjectsController subjectsController = fxmlLoader.getController();
            subjectsController.setLoggedInUser(loggedInUsername); // Pass the logged-in user

            // Show the new page in the current window
            Stage stage = (Stage) performanceButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Subjects");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "An error occurred while navigating to the Subjects page: " + e.getMessage());
        }
    }


    // Linking FXML components with TaskManager.fxml
    @FXML
    private TextField taskNameField;  // Text field for task name

    @FXML
    private ComboBox<String> categoryComboBox;  // ComboBox for category selection (Work, Personal, etc.)

    @FXML
    private DatePicker taskDatePicker;  // Date picker for task date

    @FXML
    private TextField taskTimeField;  // Text field for task time (HH:mm)

    @FXML
    private ComboBox<String> reminderComboBox;

    @FXML
    private ComboBox<String> priorityComboBox;

    // Method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);  // You can set a header text or leave it null
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        notificationManager = new NotificationManager();
        // Add items to the ComboBox when the controller is initialized
        categoryComboBox.setItems(FXCollections.observableArrayList("Work", "Personal", "Significant Date"));

        reminderComboBox.setItems(FXCollections.observableArrayList(
                "No Reminder",
                "15 Minutes Before",
                "30 Minutes Before",
                "1 Hour Before",
                "2 Hours Before",
                "1 Day Before"
        ));

        // Add items to the priority ComboBox
        priorityComboBox.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));
    }
    @FXML
    private void handleAddTask() {
        // Get user input from the form
        String taskName = taskNameField.getText();
        String category = categoryComboBox.getValue();
        LocalDate taskDate = taskDatePicker.getValue();
        String taskTime = taskTimeField.getText();
        String reminder = reminderComboBox.getValue();
        String selectedPriority = priorityComboBox.getValue();

        // Check for required fields
        if (taskName.isEmpty() || category == null || taskDate == null || taskTime.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        // Validate the date to ensure it is not in the past
        if (taskDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a valid future date for the task.");
            return;
        }

        // Validate the time input (HH:mm)
        LocalTime selectedTime;
        try {
            selectedTime = LocalTime.parse(taskTime, DateTimeFormatter.ofPattern("HH:mm"));

            // If the task date is today, ensure the time is in the future
            if (taskDate.equals(LocalDate.now())) {
                if (selectedTime.isBefore(LocalTime.now())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please select a time that is in the future.");
                    return;
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid time (HH:mm).");
            return;
        }

        // Retrieve the currently logged-in user
        String loggedInUser = LoginController.loggedInUsername; // Assuming this variable is defined in LoginController

        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user is logged in.");
            return;
        }

        // Calculate the reminder time
        LocalDateTime reminderTime = calculateReminderTime(taskDate, selectedTime, reminder);
        Timestamp sqlReminderTime = reminderTime != null ? Timestamp.valueOf(reminderTime) : null; // Move this outside the if block

        // SQL query to insert task, including reminder time
        String insertTaskQuery = "INSERT INTO tasks (username, task_name, category, task_date, task_time, reminder, priority, reminder_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Execute database query
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertTaskQuery)) {

            // Set query parameters
            stmt.setString(1, loggedInUser);
            stmt.setString(2, taskName);
            stmt.setString(3, category);
            stmt.setDate(4, java.sql.Date.valueOf(taskDate));
            stmt.setString(5, taskTime);
            stmt.setString(6, reminder != null ? reminder : "No Reminder");
            stmt.setString(7, selectedPriority);
            stmt.setTimestamp(8, sqlReminderTime); // Store reminder time

            // Execute the update
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Task Added", "Your task has been added successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while saving the task: " + e.getMessage());
        }
    }

    // Calculate reminder time based on user selection
    private LocalDateTime calculateReminderTime(LocalDate taskDate, LocalTime selectedTime, String reminder) {
        LocalDateTime taskDateTime = LocalDateTime.of(taskDate, selectedTime);
        switch (reminder) {
            case "15 Minutes Before":
                return taskDateTime.minusMinutes(15);
            case "30 Minutes Before":
                return taskDateTime.minusMinutes(30);
            case "1 Hour Before":
                return taskDateTime.minusHours(1);
            case "2 Hours Before":
                return taskDateTime.minusHours(2);
            case "1 Day Before":
                return taskDateTime.minusDays(1);
            default:
                return null; // No reminder set
        }
    }
}
