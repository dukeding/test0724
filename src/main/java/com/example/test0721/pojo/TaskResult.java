package com.example.test0721.pojo;

public class TaskResult {
    private String taskId;
    private Task.TaskStatusEnum result;
    private Object resultInfo;

    public Task.TaskStatusEnum getResult() {
        return result;
    }

    public void setResult(Task.TaskStatusEnum result) {
        this.result = result;
    }

    public Object getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(Object resultInfo) {
        this.resultInfo = resultInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "taskId='" + taskId + '\'' +
                ", result=" + result +
                ", resultInfo=" + resultInfo +
                '}';
    }
}
