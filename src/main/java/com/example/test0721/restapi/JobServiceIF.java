package com.example.test0721.restapi;

import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;

import java.util.List;

public interface JobServiceIF {
    public List<Job> getJobs();

    public List<Task> getTasks();

    public void cancelTaskByTaskId(String taskId);

    public void deleteTaskByTaskId(String taskId);
}
