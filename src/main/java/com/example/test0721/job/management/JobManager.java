package com.example.test0721.job.management;

import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import com.example.test0721.pojo.TaskResult;

import java.util.List;

public interface JobManager {

    // handle job
    public void createJob(Job job);
    public List<Job> getJobList();
    public Job getJobByJobId(String jobId);
    public Job getJobByTaskId(String taskId);
    public void updateJobMetaInfo(String jobId, String jobName, String jobScheduledTime, Long jobTimeout, String description);
    public void updateJobRuntimeInfoByJobId(String jobId, String latestTaskId, Long latestTaskRunTime, Task.TaskStatusEnum latestTaskStatus);
    public void updateJobLatestTaskStatusByTaskId(String latestTaskId, Task.TaskStatusEnum latestTaskStatus);
    public void deleteJobByJobId(String jobId);

    // handle task
    public void createTaskByJobId(String jobId);
    public List<Task> getTaskList();
    public List<Task> getTaskListByJobId(String jobId);
    public Task getTaskByTaskId(String taskId);
    public void deleteTaskByTaskId(String taskId);
    public void deleteTaskByJobId(String jobId);

    // handle running task
    public void cancelTaskByTaskId(String taskId);

    // handle task result
    public void handleTaskResult(TaskResult tr);

}
