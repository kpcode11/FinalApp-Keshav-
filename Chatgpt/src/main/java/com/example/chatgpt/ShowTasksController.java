package com.example.chatgpt;

import javafx.event.ActionEvent;  // Correct import for JavaFX
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ShowTasksController {

    @FXML
    private FlowPane tasksContainer;

    private String loggedInUser;

    // Set the logged-in user, which will be used to fetch tasks
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
        loadUserTasks();
    }

    // Load tasks from the database and display them as rectangles
    private void loadUserTasks() {
        List<Task> tasks = getUserTasksFromDatabase();  // Fetch tasks

        // For each task, create a visual representation
        for (Task task : tasks) {
            StackPane taskPane = createTaskPane(task);  // Create a rectangle for each task
            tasksContainer.getChildren().add(taskPane);  // Add the rectangle to the tasks container
        }
    }

    // Method to create a small rounded rectangle for each task
    private StackPane createTaskPane(Task task) {
        StackPane stackPane = new StackPane();

        // Create a rounded rectangle
        Rectangle rectangle = new Rectangle(150, 100);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);
        rectangle.setFill(Color.LIGHTBLUE);

        // Set the background color based on the task priority
        switch (task.getPriority()) {
            case "High":
                rectangle.setFill(Color.RED);  // Red for High priority
                break;
            case "Medium":
                rectangle.setFill(Color.YELLOW);  // Yellow for Medium priority
                break;
            case "Low":
                rectangle.setFill(Color.LIGHTGREEN);  // Green for Low priority
                break;
            default:
                rectangle.setFill(Color.LIGHTGRAY);  // Default color
        }

        // Task name and details
        Text taskText = new Text(task.getTaskName() + "\n" + task.getCategory() + "\n" + task.getTaskDate());
        taskText.setStyle("-fx-font-size: 12px;");

        stackPane.getChildren().addAll(rectangle, taskText);
        return stackPane;
    }

    // Fetch tasks from the database for the logged-in user
    private List<Task> getUserTasksFromDatabase() {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT task_name, category, task_date, priority FROM tasks WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInUser);  // Pass the logged-in user's username
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String category = rs.getString("category");
                String taskDate = rs.getString("task_date");
                String priority = rs.getString("priority");

                tasks.add(new Task(taskName, category, taskDate, priority));  // Add each task to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // Method to redirect to the Task Manager page
    @FXML
    private void redirectToTaskManager(ActionEvent event) {
        try {
            // Load the TaskManager FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskManager.fxml"));
            Parent taskManagerRoot = loader.load();

            // Get the current stage (window) and set the TaskManager scene
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(taskManagerRoot));
            currentStage.setTitle("Task Manager");
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
