package com.example.chatgpt;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {

    // Method to redirect to the performance (subjects) section
    @FXML
    private void goToPerformanceSection(ActionEvent event) {
        try {
            // Load the subjects.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subjects.fxml"));
            Parent subjectsRoot = loader.load();

            // Pass the logged-in user to SubjectsController (optional, if needed)
            SubjectsController subjectsController = loader.getController();
            subjectsController.setLoggedInUser(loggedInUsername);  // You can replace "loggedInUsername" with the actual username

            // Get the current stage (window) and set the new scene
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(subjectsRoot));
            currentStage.setTitle("Subjects Performance");
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    @FXML
    private void goHome() {
        showAlert("Home", "This is the Home section.");
    }

    @FXML
    private void openTaskManager(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TaskManager.fxml"));
            Parent root = fxmlLoader.load();

            TaskManagerController taskManagerController = fxmlLoader.getController();
            taskManagerController.setLoggedInUsername(loggedInUsername);  // Pass the logged-in username to TaskManager

//            Stage stage = new Stage();
//            stage.setTitle("Task Manager");
//            stage.setScene(new Scene(root));
//            stage.show();
            // Get the current stage (window) and set the new scene
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Subjects Performance");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAttendance() {
        showAlert("Attendance", "This is the Attendance section.");
    }

    @FXML
    private void openPerformance() {
        showAlert("Performance", "This is the Performance section.");
    }

    @FXML
    private void logOut() {
        // Close the current window (dashboard)
        Stage stage = (Stage) Stage.getWindows().get(0);
        stage.close();

        // Redirect to login page after logging out
        LoginController loginController = new LoginController();
        loginController.openLoginPage();
    }

    // Helper method to show a simple alert for now (you can later redirect to actual pages)
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private BarChart<String, Number> marksBarChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    public void initialize() {
        loadChartData();
    }

    private void loadChartData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Marks");

        String query = "SELECT subject_name, marks FROM subjects WHERE username = ?"; // Use the logged-in user

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, LoginController.loggedInUsername); // Pass the logged-in user
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                int marks = rs.getInt("marks");
                series.getData().add(new XYChart.Data<>(subjectName, marks)); // Add data to the series
            }

            marksBarChart.getData().add(series); // Add the series to the chart

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally show an alert for database errors
        }
    }
}
