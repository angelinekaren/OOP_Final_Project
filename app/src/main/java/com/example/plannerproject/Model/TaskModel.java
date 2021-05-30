package com.example.plannerproject.Model;


// Task Model for creating tasks
public class TaskModel {
    private String task, dateTime, clockTime, taskId, priority;
    private int status;

    // Empty constructor
    public TaskModel() {}

    // Constructor
    public TaskModel(String taskId, String task, String dateTime, String clockTime, int status, String priority) {
        this.taskId = taskId;
        this.task = task;
        this.dateTime = dateTime;
        this.clockTime = clockTime;
        this.status = status;
        this.priority = priority;
    }

    // Getter

    // Get Task id
    public String getTaskId() {
        return taskId;
    }
    // Get task title
    public String getTask() {
        return task;
    }
    // Get date time
    public String getDateTime() {
        return dateTime;
    }
    // Get clock time
    public String getClockTime() {
        return clockTime;
    }
    // Get priority
    public String getPriority() {
        return priority;
    }
    // Get status
    public int getStatus() {
        return status;
    }

}
