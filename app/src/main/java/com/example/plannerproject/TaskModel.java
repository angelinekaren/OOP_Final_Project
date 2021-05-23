package com.example.plannerproject;

import java.util.Map;

public class TaskModel {
    private String task, dateTime, clockTime, taskId;
    private int status;

    public TaskModel() {}

    public TaskModel(String taskId, String task, String dateTime, String clockTime, int status) {
        this.taskId = taskId;
        this.task = task;
        this.dateTime = dateTime;
        this.clockTime = clockTime;
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

    public String getClockTime() {
        return clockTime;
    }

    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
