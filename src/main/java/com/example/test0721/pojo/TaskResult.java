package com.example.test0721.pojo;

public class TaskResult {
    public enum TaskResultEnum {
        SUCCESS, SUCCESS_WITHWARNING, FAILED, INTERRUPTED
    }

    private String taskId;
    private TaskResultEnum result;
    private Long endTime;
    private Object resultInfo;

    public TaskResultEnum getResult() {
        return result;
    }

    public void setResult(TaskResultEnum result) {
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

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "taskId='" + taskId + '\'' +
                ", result=" + result +
                ", endTime=" + endTime +
                ", resultInfo=" + resultInfo +
                '}';
    }
}
