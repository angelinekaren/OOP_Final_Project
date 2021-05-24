package com.example.plannerproject;


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

    // Setter and getter

    // Get Task id
    public String getTaskId() {
        return taskId;
    }
    // Set Task id
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    // Get task title
    public String getTask() {
        return task;
    }
    // Set task title
    public void setTask(String task) {
        this.task = task;
    }
    // Get date time
    public String getDateTime() {
        return dateTime;
    }
    // Set date time
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    // Get clock time
    public String getClockTime() {
        return clockTime;
    }
    // Set priority
    public void setPriority(String priority) {
        this.priority = priority;
    }
    // Get priority
    public String getPriority() {
        return priority;
    }
    // Set clock time
    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }
    // Get status
    public int getStatus() {
        return status;
    }
    // Set status
    public void setStatus(int status) {
        this.status = status;
    }
}
