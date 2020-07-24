package com.example.test0721.pojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Task implements Cloneable{
    private Logger logger = LoggerFactory.getLogger(Task.class);

    // meta info
    private String taskId;
    private String jobId;
    private String taskName;
    private Long timeout;
    private String description;

    // runtime info
    private Long startTime;
    private Long endTime;
    private TaskStatusEnum taskStatus;
    private Object resultInfo;

    public Task() {
    }

    public Task(String taskId, String jobId, String taskName, Long timeout, String description, Long startTime, Long endTime, TaskStatusEnum taskStatus, Object resultInfo) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.taskName = taskName;
        this.timeout = timeout;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskStatus = taskStatus;
        this.resultInfo = resultInfo;
    }

    public enum TaskStatusEnum {
        NONE,
        READY,
        INITIALIZING,
        RUNNING,
        TIMEOUT,
        CANCELLED,
        COMPLETED_SUCCESS,
        COMPLETE_SUCCESSWITHWARNING,
        COMPLETE_FAILED;
    }

    public static boolean inRunning(TaskStatusEnum taskStatus) {
        switch (taskStatus) {
            case NONE:
            case READY:
            case TIMEOUT:
            case CANCELLED:
            case COMPLETED_SUCCESS:
            case COMPLETE_SUCCESSWITHWARNING:
            case COMPLETE_FAILED:
                return false;
            case INITIALIZING:
            case RUNNING:
                return true;
            default:
                throw new RuntimeException("Unrecognized job status!");
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public TaskStatusEnum getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusEnum taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(Object resultInfo) {
        this.resultInfo = resultInfo;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Task copy() {
        Task task = null;
        try {
            task = (Task) this.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("Task copy failed", e);
        }

        return task;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", timeout=" + timeout +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", taskStatus=" + taskStatus +
                ", resultInfo=" + resultInfo +
                '}';
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Task.class);

        logger.info("OK");

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            logger.info("async start");
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                logger.info("sleep in async Interrupted");
            }
            logger.info("sleep in async finished");
            return 1234;
        }).orTimeout(10 * 1000, TimeUnit.MILLISECONDS);

        try {
            logger.info("haha" + future.get());
        } catch (InterruptedException e) {
            logger.info("get() in main InterruptedException");
        } catch (ExecutionException e) {
            logger.info("get() in main ExecutionException");
            logger.info(String.valueOf(future.isCancelled()));
            future.cancel(true);
        }

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            System.out.println("sleep in main interrupted");
        }
    }
}
