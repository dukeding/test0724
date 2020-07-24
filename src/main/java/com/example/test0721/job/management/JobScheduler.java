package com.example.test0721.job.management;

import com.example.test0721.execution.management.Executor;
import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DependsOn({"JobManager", "Executor"})
public class JobScheduler extends Thread {
    private static final long LOOP_INTERVAL = 20 * 1000;

    private Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    @Autowired
    private JobManager jobManager;

    @Autowired
    private Executor executor;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.start();
    }

//    private void doHandleTimeout(Job job, Task task) {
//        logger.info("doHandleTimeout");
//        job.setLatestTaskStatus(Task.TaskStatusEnum.TIMEOUT);
//        task.setEndTime(System.currentTimeMillis());
//        task.setTaskStatus(Task.TaskStatusEnum.TIMEOUT);
//
//        executor.cancelTaskByTaskId(task.getTaskId());
//    }

//    private Long getNextRunTime(String jobScheduledTime, Long lastRunTime) {
//        if (jobScheduledTime == null || jobScheduledTime.isEmpty())
//            return null;
//
//        long _lastRunTime = (lastRunTime != null) ? lastRunTime.longValue() : 0;
//
//        long currentTime = System.currentTimeMillis();
//
//        OptionalLong optional = Arrays.stream(jobScheduledTime.split(",")).mapToLong(Long::parseLong)
//                .filter(e -> e <= currentTime && (lastRunTime == null ? true : e > _lastRunTime))
//                .max(); // the max time that between lastRunTime and current time
//
//        return optional.isPresent() ? optional.getAsLong() : null;
//
////        Long result = null;
////
////        Long lastRunTime = job.getLatestTaskRunTime();
////
////        // todo: get schedule list
////        List<Long> scheduleList = Arrays.stream(job.getJobScheduledTime().split(",")).map(Long::parseLong).sorted().collect(Collectors.toList());
////
////        if (lastRunTime == null)
////            result = scheduleList.stream().findFirst().orElse(null);
////        else
////            result = scheduleList.stream().filter(e -> e > lastRunTime).findFirst().orElse(null);
////
////        return result;
//    }

    private void doLoop() {
//        long loopStartTime = System.currentTimeMillis();
//        System.out.println("JobSchedulerImpl::run: a new iteration");

        List<Job> list = jobManager.getJobList();

        // todo: parallelize
        for (Job job : list) {
            if (job == null)
                continue;

            String latestTaskId = job.getLatestTaskId();

//            // step 1: handle timeout if needed
//            if (latestTaskId != null && !latestTaskId.isEmpty()) {
//                Task task = jobManager.getTaskByTaskId(latestTaskId);
//                if (Task.inRunning(task.getTaskStatus()) && task.getStartTime() + job.getJobTimeout() > System.currentTimeMillis()) { // should timeout
//                    doHandleTimeout(job, task);
//                }
//            }

            // step 2: handle invocation if needed
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
                String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                Task task = new Task(taskId
                        , job.getJobId()
                        , "This is task name"
                        , job.getJobTimeout()
                        , "This is task description"
                        , System.currentTimeMillis()
                        , null
                        , Task.TaskStatusEnum.RUNNING // initialized as "RUNNING"
                        , null
                        );

                jobManager.updateJobRuntimeInfoByJobId(job.getJobId(), taskId, optional.getAsLong(), Task.TaskStatusEnum.RUNNING);

                executor.startNewTaskByTaskId(task.getTaskId(), job.getJobTimeout(), job);


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
