package com.example.test0721.job.management;

import com.example.test0721.execution.management.Executor;
import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import com.example.test0721.pojo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "JobManager")
public class JobManagerImpl implements JobManager {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    Executor executor;

    private List<Job> jobList = new ArrayList<>(Arrays.asList(new Job("xxx", "job1",
            String.valueOf(System.currentTimeMillis() + 10 * 1000) + "," + String.valueOf(System.currentTimeMillis() + 40 * 1000),
            30 * 1000L, "job1 description", null, null, null)));

    private List<Task> taskList = new ArrayList<>();

    private String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    @Override
    public synchronized void createJob(Job job) {
        if (job == null)
            return;

        if (job.getJobId() == null || job.getJobId().isEmpty())
            job.setJobId(generateUUID());

        jobList.add(job.copy());
    }

    @Override
    public synchronized List<Job> getJobList() {
        return jobList.parallelStream().map(e -> e.copy()).sorted(Comparator.comparing(Job::getJobScheduledTime)
                .reversed()).collect(Collectors.toList());
    }

    @Override
    public synchronized Job getJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().map(e -> e.copy()).orElse(null);
    }

    @Override
    public synchronized void updateJobMetaInfo(String jobId, String jobName, String jobScheduledTime, Long jobTimeout, String description) {
        if (jobId == null || jobId.isEmpty())
            return;

        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> {
            e.setJobName(jobName);
            e.setJobScheduledTime(jobScheduledTime);
            e.setJobTimeout(jobTimeout);
            e.setDescription(description);
        });
    }

//    @Override
//    public void updateJobLatestTaskStatusByTaskId(String latestTaskId, Task.TaskStatusEnum latestTaskStatus) {
//        if (latestTaskId == null || latestTaskId.isEmpty())
//            return;
//
//        Job job = getJobByTaskId_internal(latestTaskId);
//
//        if (job == null || !latestTaskId.equals(job.getLatestTaskId()))
//            return;
//
//        job.setLatestTaskStatus(latestTaskStatus);
//    }

    private synchronized Job getJobByJobId_internal(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().orElse(null);
    }

    @Override
    public synchronized Job getJobByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        String jobId = taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().map(e -> e.getJobId()).orElse(null);

        if (jobId == null || jobId.isEmpty())
            return null;

        Job job = jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().orElse(null);

        return job;
    }

    @Override
    public synchronized void handleTaskResult(TaskResult tr, String jobId) {
        if (tr == null || tr.getTaskId() == null || tr.getTaskId().isEmpty())
            return;

        logger.info(tr.toString());

        // update task
        String taskId = tr.getTaskId();

        Task task = getTaskByTaskId_internal(taskId);
        if (task != null && Task.inRunning(task.getTaskStatus())) { // task still in running (not timeout, cancelled)
            task.setEndTime(System.currentTimeMillis());
            task.setTaskStatus(tr.getResult());
            task.setResultInfo(tr.getResultInfo());
        }

        // update job
        Job job = getJobByJobId_internal(jobId); // do not use taskId to lookup job, because task may be already deleted

        if (job != null && job.getLatestTaskId() != null && job.getLatestTaskId().equals(taskId)) {
            if (Task.inRunning(job.getLatestTaskStatus())) { // job still in running
                job.setLatestTaskStatus(tr.getResult());
            }
        }
    }

    private synchronized Job getJobByTaskId_internal(String taskId) {
        Task task = getTaskByTaskId_internal(taskId);
        if (task == null || task.getJobId() == null || task.getJobId().isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> task.getJobId().equals(e.getJobId())).findAny().orElse(null);
    }

    private synchronized Task getTaskByTaskId_internal(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().orElse(null);
    }

    @Override
    public synchronized void deleteJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        taskList.parallelStream().filter(e -> jobId.equals(e.getJobId()) && Task.inRunning(e.getTaskStatus()))
                .forEach(e -> cancelTaskByTaskId(e.getTaskId()));

        doDeleteTaskByJobId(jobId);

        doDeleteJobByJobId(jobId);
    }

    @Override
    public synchronized void cancelTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return;

        // cancel running task
        executor.cancelTaskByTaskId(taskId);

//        // update job
//        Job job = getJobByTaskId_internal(taskId);
//
//        if (job == null) // not found
//            return;
//
//        if (Task.inRunning(job.getLatestTaskStatus()))
//            job.setLatestTaskStatus(Task.TaskStatusEnum.CANCELLED);
//
//        // update task if task still in running
//        taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().ifPresent(e -> {
//            if (Task.inRunning(e.getTaskStatus())) {
//                e.setTaskStatus(Task.TaskStatusEnum.CANCELLED);
//                e.setEndTime(System.currentTimeMillis());
//            }
//        });
    }

    private synchronized void doDeleteTaskByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        // below code would throw ConcurrentModificationException
        //taskList.parallelStream().filter(e -> jobId.equals(e.getJobId())).forEach(e -> taskList.remove(e));

        Iterator<Task> iterable = taskList.iterator();
        while (iterable.hasNext()) {
            Task task = iterable.next();
            if (jobId.equals(task.getJobId())) {
                iterable.remove();
            }
        }

    }

    private synchronized void doDeleteJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> jobList.remove(e));
    }

    @Override
    public synchronized String createTaskByJobId(String jobId, Long jobTimeout) {
        if (jobId == null || jobId.isEmpty())
            return null;

        // create task
        String taskId = generateUUID();
        Task task = new Task(taskId,
                jobId,
                "This is task name",
                jobTimeout,
                "This is task description",
                System.currentTimeMillis(),
                null,
                Task.TaskStatusEnum.RUNNING,
                null);

        // insert into task list
        taskList.add(task);

        // update job
        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> {
            e.setLatestTaskId(task.getTaskId());
            e.setLatestTaskRunTime(task.getStartTime());
            e.setLatestTaskStatus(task.getTaskStatus());
        });

        return taskId;
    }

    private synchronized List<Task> getTaskList_internal() {
        return taskList;
    }

    @Override
    public synchronized List<Task> getTaskList() {
        return taskList.parallelStream().map(e -> e.copy())
                .sorted(Comparator.comparing(Task::getStartTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public synchronized List<Task> getTaskListByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> jobId.equals(e.getJobId())).map(e -> e.copy())
                .sorted(Comparator.comparing(Task::getStartTime)).collect(Collectors.toList());
    }

    @Override
    public synchronized Task getTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny()
                .map(e -> e.copy()).orElse(null);
    }

    @Override
    public synchronized void deleteTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return;

        taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().ifPresent(e -> {
            if (Task.inRunning(e.getTaskStatus()))
                cancelTaskByTaskId(taskId);

            // cancel would update task and job, but the async remove() operation may make task/job not found
            // for task, if the task is removed, then no need to update task
            // for job, note that don't use taskId to look up job
            taskList.remove(e);
        });
    }

//    private synchronized void deleteTaskByJobId(String jobId) {
//        if (jobId == null || jobId.isEmpty())
//            return;
//
//        Iterator<Task> iterator = taskList.iterator();
//        while (iterator.hasNext()) {
//            Task task = iterator.next();
//            if (jobId.equals(task.getJobId())) {
//                if (Task.inRunning(task.getTaskStatus()))
//                    cancelTaskByTaskId(task.getTaskId());
//                iterator.remove();
//            }
//        }
//    }
}
