package com.example.test0721.pojo;

import com.example.test0721.job.management.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Job implements Cloneable{
    private Logger logger = LoggerFactory.getLogger(Job.class);

    // meta info
    private String jobId;
    private String jobName;
    private String jobScheduledTime;
    private Long jobTimeout;
    private String description;

    // runtime info
    private String latestTaskId;
    private Long latestTaskRunTime;
    private Task.TaskStatusEnum latestTaskStatus;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Long getJobTimeout() {
        return jobTimeout;
    }

    public void setJobTimeout(Long jobTimeout) {
        this.jobTimeout = jobTimeout;
    }

    public Long getLatestTaskRunTime() {
        return latestTaskRunTime;
    }

    public void setLatestTaskRunTime(Long latestTaskRunTime) {
        this.latestTaskRunTime = latestTaskRunTime;
    }

    public String getLatestTaskId() {
        return latestTaskId;
    }

    public void setLatestTaskId(String latestTaskId) {
        this.latestTaskId = latestTaskId;
    }

    public Task.TaskStatusEnum getLatestTaskStatus() {
        return latestTaskStatus;
    }

    public void setLatestTaskStatus(Task.TaskStatusEnum latestTaskStatus) {
        this.latestTaskStatus = latestTaskStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobScheduledTime() {
        return jobScheduledTime;
    }

    public void setJobScheduledTime(String jobScheduledTime) {
        this.jobScheduledTime = jobScheduledTime;
    }

    public Job(String jobId, String jobName, String jobScheduledTime, Long jobTimeout, String description, String latestTaskId, Long latestTaskRunTime, Task.TaskStatusEnum latestTaskStatus) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobScheduledTime = jobScheduledTime;
        this.jobTimeout = jobTimeout;
        this.description = description;
        this.latestTaskId = latestTaskId;
        this.latestTaskRunTime = latestTaskRunTime;
        this.latestTaskStatus = latestTaskStatus;
    }

    public Job() {
    }

    public Job copy() {
        Job job = null;

        try {
            job = (Job) this.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("Job copy failed", e);
        }
        return job;
    }

//    public void updateRuntimeInfo(Job job) {
//        if (job == null)
//            return;
//
//        // todo:
//        this.latestTaskId = job.getLatestTaskId();
//        this.latestTaskStatus = job.getLatestTaskStatus();
//        this.latestTaskRunTime = job.getLatestTaskRunTime();
//    }

    public void updateMetaInfo(Job job) {
        if (job == null)
            return;

        // todo:
        //this.jobId = job.getJobId();
        this.jobName = job.getJobName();
        this.jobScheduledTime = job.getJobScheduledTime();
        this.jobTimeout = job.getJobTimeout();
        this.description = job.getDescription();
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobScheduledTime='" + jobScheduledTime + '\'' +
                ", jobTimeout=" + jobTimeout +
                ", description='" + description + '\'' +
                ", latestTaskId='" + latestTaskId + '\'' +
                ", latestTaskRunTime=" + latestTaskRunTime +
                ", latestTaskStatus=" + latestTaskStatus +
                '}';
    }

    public static void main(String[] args) {
        Job job = new Job("xxx", "jobname", "aaa", 60*1000L, "description", null, null, null);
        Job job2 = null;
        try {
            job2 = (Job)job.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        System.out.println(job.getJobId());
        System.out.println(job2.getJobId());

        job2.setJobId("yyy");

        System.out.println(job.getJobId());
        System.out.println(job2.getJobId());

    }
}
