package com.example.chatgpt;

import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationManager {

    private ScheduledExecutorService scheduler;

    public NotificationManager() {
        scheduler = Executors.newScheduledThreadPool(1);
        startNotificationCheck();
    }

    private void startNotificationCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            // Fetch tasks that require reminders
            List<Task> tasks = getTasksForReminders();
            for (Task task : tasks) {
                showReminderNotification(task);
            }
        }, 0, 1, TimeUnit.MINUTES); // Check every minute
    }

    private List<Task> getTasksForReminders() {
        List<Task> tasksToRemind = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now(); // Get the current time

        // SQL query to fetch tasks with reminders that are due
        String query = "SELECT task_name, category, task_date, task_time, reminder, priority " +
                "FROM tasks WHERE reminder_time <= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the current time as a parameter for the query
            stmt.setTimestamp(1, Timestamp.valueOf(currentTime)); // Use the current timestamp

            ResultSet rs = stmt.executeQuery();

            // Iterate through the results and create Task objects
            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String category = rs.getString("category");
                LocalDate taskDate = rs.getDate("task_date").toLocalDate(); // Convert SQL date to LocalDate
                String taskTime = rs.getString("task_time");
                String reminder = rs.getString("reminder");
                String priority = rs.getString("priority");

                // Create a Task object and add it to the list
                tasksToRemind.add(new Task(taskName, category, taskDate.toString(), priority));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions that occur
        }

        return tasksToRemind; // Return the list of tasks that need reminders
    }



    private void showReminderNotification(Task task) {
        // Show a reminder notification to the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Reminder");
        alert.setHeaderText("Reminder for Task: " + task.getTaskName());
        alert.setContentText("Don't forget: " + task.getTaskName() + " is due on " + task.getTaskDate() + "!");
        alert.showAndWait();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
