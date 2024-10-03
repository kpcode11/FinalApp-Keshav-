package com.example.chatgpt;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.paint.Color; // Import for color management

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceController {

    private String loggedInUsername;

    public void setLoggedInUser(String username) {
        this.loggedInUsername = username;
        // Load subjects when the user is set
        loadSubjectsForUser();
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/chatgpt/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUsername(loggedInUsername);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(dashboardRoot));
            currentStage.setTitle("Dashboard");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TextField subjectNameField;

    @FXML
    private TextField minPercentageField;

    @FXML
    private VBox subjectList;

    @FXML
    private Label totalAttendanceLabel; // Define the label for total attendance

    private List<AttendanceSubject> subjects = new ArrayList<>();

    @FXML
    public void initialize() {
        // Initialization logic can be added here if needed
    }

    @FXML
    private void handleAddSubject() {
        String subjectName = subjectNameField.getText();
        String minPercentage = minPercentageField.getText();

        if (subjectName.isEmpty() || minPercentage.isEmpty()) {
            showAlert("Error", "Subject name and minimum percentage cannot be empty.");
            return;
        }

        try {
            int minPercentageValue = Integer.parseInt(minPercentage);
            if (minPercentageValue < 0) {
                showAlert("Error", "Minimum percentage cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for the minimum percentage.");
            return;
        }

        // Insert the subject into the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO user_subjects (subject_name, min_percentage, username) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectName);
            stmt.setString(2, minPercentage);
            stmt.setString(3, loggedInUsername);  // Store the logged-in user's username
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add subject to the database.");
        }

        subjectNameField.clear();
        minPercentageField.clear();
        loadSubjectsForUser(); // Refresh the subject list
    }

    public void loadSubjectsForUser() {
        subjectList.getChildren().clear();  // Clear the current list
        subjects.clear(); // Clear the list of subjects

        String query = "SELECT subject_name, min_percentage FROM user_subjects WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInUsername);  // Fetch subjects for the logged-in user
            ResultSet rs = stmt.executeQuery(); // Ensure this line executes correctly

            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                int minPercentage = rs.getInt("min_percentage");

                // Create an AttendanceSubject object for this subject
                AttendanceSubject subject = new AttendanceSubject(subjectName, minPercentage);
                subject.calculateAttendancePercentage(); // Call to populate attended and totalClasses
                subjects.add(subject); // Store the subject for later use

                // Create a UI element for the subject
                HBox subjectRow = new HBox(10);
                subjectRow.setAlignment(Pos.CENTER_LEFT);

                // Change font color of subject name label to white
                Label subjectNameLabel = new Label(subject.getName() + " (Min: " + subject.getMinPercentage() + "%)");
                subjectNameLabel.setTextFill(Color.WHITE); // Set font color to white

                // Create buttons with specified background color and text color
                Button presentButton = new Button("Present");
                presentButton.setStyle("-fx-background-color: #281352; -fx-text-fill: white;"); // Set background color and text color

                Button absentButton = new Button("Absent");
                absentButton.setStyle("-fx-background-color: #281352; -fx-text-fill: white;"); // Set background color and text color

                // Create attendance percentage label and set font color to white
                Label attendancePercentageLabel = new Label("Attendance: " + subject.getAttendancePercentage() + "%");
                attendancePercentageLabel.setTextFill(Color.WHITE); // Set font color to white

                // Change font color based on attendance percentage
                updateAttendanceLabelColor(attendancePercentageLabel, subject.getAttendancePercentage(), subject.getMinPercentage());

                // Button actions remain the same
                presentButton.setOnAction(e -> {
                    subject.markPresent();
                    storeAttendance(subject.getName(), true); // Store attendance record
                    attendancePercentageLabel.setText("Attendance: " + subject.getAttendancePercentage() + "%");
                    updateAttendanceLabelColor(attendancePercentageLabel, subject.getAttendancePercentage(), subject.getMinPercentage());
                    updateTotalAttendancePercentage();  // Update total percentage when present is clicked
                });

                absentButton.setOnAction(e -> {
                    subject.markAbsent();
                    storeAttendance(subject.getName(), false); // Store attendance record
                    attendancePercentageLabel.setText("Attendance: " + subject.getAttendancePercentage() + "%");
                    updateAttendanceLabelColor(attendancePercentageLabel, subject.getAttendancePercentage(), subject.getMinPercentage());
                    updateTotalAttendancePercentage();  // Update total percentage when absent is clicked
                });

                // Add all components to the subject row
                subjectRow.getChildren().addAll(subjectNameLabel, presentButton, absentButton, attendancePercentageLabel);
                subjectList.getChildren().add(subjectRow);  // Add the row to the VBox
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load subjects."); // Handle SQL exception
        }

        updateTotalAttendancePercentage(); // Calculate initial total percentage
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to update total attendance percentage
    private void updateTotalAttendancePercentage() {
        int totalAttended = 0;
        int totalClasses = 0;

        for (AttendanceSubject subject : subjects) {
            totalAttended += subject.getAttended(); // Ensure this method exists
            totalClasses += subject.getTotalClasses(); // Ensure this method exists
        }

        int totalPercentage = (totalClasses == 0) ? 0 : (totalAttended * 100) / totalClasses;
        totalAttendanceLabel.setText("Total Attendance: " + totalPercentage + "%");

        // Change total attendance label color if below 75%
        if (totalPercentage < 75) {
            totalAttendanceLabel.setTextFill(Color.RED);
        } else {
            totalAttendanceLabel.setTextFill(Color.WHITE); // Reset color to black if above 75%
        }
    }

    @FXML
    private void refreshSubjectList() {
        subjectList.getChildren().clear();  // Clear the list before re-populating

        for (AttendanceSubject subject : subjects) {
            HBox subjectRow = new HBox(10);
            subjectRow.setAlignment(Pos.CENTER_LEFT);

            Label subjectNameLabel = new Label(subject.getName() + " (Min: " + subject.getMinPercentage() + "%)");
            Button presentButton = new Button("Present");
            Button absentButton = new Button("Absent");

            // Button actions
            presentButton.setOnAction(e -> {
                subject.markPresent();
                storeAttendance(subject.getName(), true); // Store attendance record
                refreshSubjectList();  // Refresh after marking
            });

            absentButton.setOnAction(e -> {
                subject.markAbsent();
                storeAttendance(subject.getName(), false); // Store attendance record
                refreshSubjectList();  // Refresh after marking
            });

            // Display attendance percentage
            Label attendancePercentageLabel = new Label("Attendance: " + subject.getAttendancePercentage() + "%");
            updateAttendanceLabelColor(attendancePercentageLabel, subject.getAttendancePercentage(), subject.getMinPercentage());

            subjectRow.getChildren().addAll(subjectNameLabel, presentButton, absentButton, attendancePercentageLabel);

            subjectList.getChildren().add(subjectRow);  // Add each subject row to the list
        }
    }

    // Method to update label color based on attendance percentage
    private void updateAttendanceLabelColor(Label attendanceLabel, int attendancePercentage, int minPercentage) {
        if (attendancePercentage < minPercentage) {
            attendanceLabel.setTextFill(Color.RED); // Change color to red
        } else {
            attendanceLabel.setTextFill(Color.BLACK); // Reset to black if above minimum
        }
    }

    // Method to store attendance in the database
    private void storeAttendance(String subjectName, boolean attended) {
        String query = "INSERT INTO attendance_records (username, subject_name, attended, date) VALUES (?, ?, ?, CURDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInUsername);
            stmt.setString(2, subjectName);
            stmt.setBoolean(3, attended);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to record attendance.");
        }
    }

    // Inner class to represent attendance details of a subject
    class AttendanceSubject {
        private String name;
        private int minPercentage;
        private int attended;
        private int totalClasses;

        public AttendanceSubject(String name, int minPercentage) {
            this.name = name;
            this.minPercentage = minPercentage;
            this.attended = 0;
            this.totalClasses = 0;
        }

        public void calculateAttendancePercentage() {
            String query = "SELECT COUNT(*) as total, SUM(CASE WHEN attended THEN 1 ELSE 0 END) as attended FROM attendance_records WHERE username = ? AND subject_name = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, loggedInUsername);
                stmt.setString(2, name); // name is the subject's name
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    totalClasses = rs.getInt("total");
                    attended = rs.getInt("attended");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

        public int getMinPercentage() {
            return minPercentage;
        }

        public void markPresent() {
            attended++;
            totalClasses++;
        }

        public void markAbsent() {
            totalClasses++;
        }

        public int getAttendancePercentage() {
            if (totalClasses == 0) return 0;
            return (attended * 100) / totalClasses;
        }

        // Getter methods for attended and totalClasses
        public int getAttended() {
            return attended;
        }

        public int getTotalClasses() {
            return totalClasses;
        }
    }
}
