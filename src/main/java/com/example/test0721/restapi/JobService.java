package com.example.test0721.restapi;

import com.example.test0721.job.management.JobManager;
import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService implements JobServiceIF {
    @Autowired
    JobManager jm;

    @Override
    public List<Job> getJobs() {
        return jm.getJobList();
    }

    @Override
    public List<Task> getTasks() {
        return jm.getTaskList();
    }

    @Override
    public void cancelTaskByTaskId(String taskId) {
        jm.cancelTaskByTaskId(taskId);
    }

    @Override
    public void deleteTaskByTaskId(String taskId) {
        jm.deleteTaskByTaskId(taskId);
    }
}
