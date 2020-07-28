package com.example.test0721.restapi;

import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobController {
    @Autowired
    JobServiceIF rs;

    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public List<Job> getJobs() {
        return rs.getJobs();
    }

    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public List<Task> getTasks() {
        return rs.getTasks();
    }

    @RequestMapping(value = "/canceltask/{task_id}", method = RequestMethod.PUT)
    public void cancelTask(@PathVariable String task_id) {
        rs.cancelTaskByTaskId(task_id);
    }

    @RequestMapping(value = "/deletetask/{task_id}", method = RequestMethod.DELETE)
    public void deleteTask(@PathVariable String task_id) {
        rs.deleteTaskByTaskId(task_id);
    }

    @RequestMapping(value = "/deletejob/{job_id}", method = RequestMethod.DELETE)
    public void deleteJob(@PathVariable String job_id) {
        rs.deleteJobByJobId(job_id);
    }

}
