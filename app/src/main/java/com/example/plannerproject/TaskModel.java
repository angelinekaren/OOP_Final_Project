package com.example.plannerproject;

public class TaskModel {
    private String task, dateTime, userId, taskId;
    private int status;

    public TaskModel() {}

    public TaskModel(String taskId, String task, String dateTime, int status) {
        this.taskId = taskId;

        this.task = task;
        this.dateTime = dateTime;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
