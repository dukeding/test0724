package com.example.test0721.job.management;

import com.example.test0721.execution.management.Executor;
import com.example.test0721.pojo.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@DependsOn({"JobManager", "Executor"})
public class JobScheduler extends Thread {
    private static final long LOOP_INTERVAL = 10 * 1000;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JobManager jobManager;

    @Autowired
    private Executor executor;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.start();
    }

    private void doLoop() {
        List<Job> list = jobManager.getJobList();

        // todo: parallelize
        for (Job job : list) {
            if (job == null)
                continue;

            String latestTaskId = job.getLatestTaskId();

            Long lastRunTime = job.getLatestTaskRunTime();
            long currentTime = System.currentTimeMillis();
            OptionalLong optional = OptionalLong.empty();
            if (lastRunTime == null) {
                optional = Arrays.stream(job.getJobScheduledTime().split(",")).mapToLong(Long::parseLong)
                        .filter(e -> e <= currentTime)
                        .max(); // the max time that before current time
            } else {
                long _lastRunTime = lastRunTime;
                optional = Arrays.stream(job.getJobScheduledTime().split(",")).mapToLong(Long::parseLong)
                        .filter(e -> e > _lastRunTime && e <= currentTime)
                        .max(); // the max time that between lastRunTime and current time
            }

            if (optional.isPresent()) { // should invoke
                String taskId = jobManager.createTaskByJobId(job.getJobId(), job.getJobTimeout());

                executor.startNewTaskByTaskId(taskId, job.getJobTimeout(), job);

                // commented out because concurrent updates may conflict
                //jobManager.updateTaskStatusByTaskId(taskId, Task.TaskStatusEnum.RUNNING);
                //jobManager.updateJobLatestTaskStatusByTaskId(taskId, Task.TaskStatusEnum.RUNNING);

            }
        }
    }

    @Override
    public void run() {
        while (true) {
            long loopStartTime = System.currentTimeMillis();
            logger.info("run() a new iteration");

            doLoop();

            logger.info("run() iteration finished");

            long loopEndTime = System.currentTimeMillis();
            long duration = loopEndTime - loopStartTime;
            if (duration >= 0 && duration < LOOP_INTERVAL) {
                try {
                    Thread.sleep(LOOP_INTERVAL - duration);
                } catch (InterruptedException e) {
                    logger.info("Got interrupt signal", e);
                }
            }
        }

    }
}
