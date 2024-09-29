package com.example.chatgpt;

public class Task {
    private String taskName;
    private String category;
    private String taskDate;
    private String priority;

    // Constructor
    public Task(String taskName, String category, String taskDate, String priority) {
        this.taskName = taskName;
        this.category = category;
        this.taskDate = taskDate;
        this.priority = priority;
    }

    // Getters
    public String getTaskName() {
        return taskName;
    }

    public String getCategory() {
        return category;
    }

    public String getTaskDate() {
        return taskDate;
    }
    public String getPriority() {return priority;}
}
