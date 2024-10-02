package com.example.chatgpt;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.example.chatgpt.LoginController.loggedInUsername;

public class SubjectsController {

    @FXML
    private TextField subjectNameField;

    @FXML
    private TextField marksField;

    @FXML
    private VBox subjectListContainer;

    @FXML
    private Label totalMarksLabel;

    @FXML
    private Label totalPercentageLabel;

    private String loggedInUser;  // Store the logged-in user
    private List<Subject> subjects = new ArrayList<>();

    // Set the logged-in user
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
        loadUserSubjects();  // Load subjects for this user
    }

    // Method to add a subject for the logged-in user
    @FXML
    private void addSubject() {
        String subjectName = subjectNameField.getText();
        String marksText = marksField.getText();

        // Validate input values (Ensure no empty fields)
        if (subjectName.trim().isEmpty() || marksText.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Both subject name and marks are required.");
            return;
        }

        try {
            int marks = Integer.parseInt(marksText);
            // Check for negative marks
            if (marks < 0) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Marks cannot be negative.");
                return;
            }

            // Add subject to the database
            addSubjectToDatabase(subjectName, marks, loggedInUsername);

            // Add subject to the list and display it in the UI
            Subject subject = new Subject(subjectName, marks);
            subjects.add(subject);
            displaySubject(subject);

            // Clear input fields
            subjectNameField.clear();
            marksField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Marks must be a valid number.");
        }
    }

    private void displaySubject(Subject subject) {
        Text subjectDisplay = new Text(subject.getName() + ": " + subject.getMarks() + " marks");

        // Set font and color
        subjectDisplay.setFont(javafx.scene.text.Font.font("Arial Black", 14)); // Set font to Arial Black
        subjectDisplay.setFill(javafx.scene.paint.Color.WHITE); // Set text color to white

        subjectListContainer.getChildren().add(subjectDisplay);
    }


    // Method to calculate total marks and percentage
    @FXML
    private void calculateResults() {
        int totalMarks = 0;
        int totalSubjects = subjects.size();

        if (totalSubjects == 0) {
            totalMarksLabel.setText("No subjects added.");
            totalPercentageLabel.setText("");
            return;
        }

        for (Subject subject : subjects) {
            totalMarks += subject.getMarks();
        }

        // Calculate percentage
        double totalPercentage = (totalMarks / (double) (totalSubjects * 100)) * 100;

        // Display total marks and percentage
        totalMarksLabel.setText("Total Marks: " + totalMarks);
        totalPercentageLabel.setText("Total Percentage: " + String.format("%.2f", totalPercentage) + "%");
    }

    private void addSubjectToDatabase(String subjectName, int marks, String username) {
        String query = "INSERT INTO subjects (username, subject_name, marks) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Print the username to debug
            System.out.println("Trying to add subject for user: " + username);

            stmt.setString(1, username); // Set the logged-in username
            stmt.setString(2, subjectName);
            stmt.setInt(3, marks);
            stmt.executeUpdate(); // Execute the insertion

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadUserSubjects() {
        subjects.clear();
        subjectListContainer.getChildren().clear();

        String query = "SELECT subject_name, marks FROM subjects WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInUser); // Fetch subjects only for the logged-in user
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                int marks = rs.getInt("marks");

                Subject subject = new Subject(subjectName, marks);
                subjects.add(subject);
                displaySubject(subject); // Display subjects in the UI
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility method to show alert dialogs
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to represent each subject
    public static class Subject {
        private final String name;
        private final int marks;

        public Subject(String name, int marks) {
            this.name = name;
            this.marks = marks;
        }

        public String getName() {
            return name;
        }

        public int getMarks() {
            return marks;
        }
    }

    @FXML
    private void redirectToDashboard(ActionEvent event) {
        try {
            // Load the TaskManager FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent taskManagerRoot = loader.load();

            // Get the current stage (window) and set the TaskManager scene
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(taskManagerRoot));
            currentStage.setTitle("Dashboard");
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
