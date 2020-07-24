package com.example.test0721.job.management;

import com.example.test0721.execution.management.Executor;
import com.example.test0721.pojo.Job;
import com.example.test0721.pojo.Task;
import com.example.test0721.pojo.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "JobManager")
public class JobManagerImpl implements JobManager {

    @Autowired
    Executor executor;

    private List<Job> jobList = new ArrayList<>(Arrays.asList(new Job("xxx", "job1",
            String.valueOf(System.currentTimeMillis() + 30 * 1000),
            60 * 1000L, "job1 description", null, null, null)));

    private List<Task> taskList = new ArrayList<>();

    private String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    @Override
    public void createJob(Job job) {
        if (job == null)
            return;

        if (job.getJobId() == null || job.getJobId().isEmpty())
            job.setJobId(generateUUID());

        jobList.add(job.copy());
    }

    @Override
    public List<Job> getJobList() {
        return jobList.parallelStream().map(e -> e.copy()).sorted(Comparator.comparing(Job::getJobScheduledTime)
                .reversed()).collect(Collectors.toList());
    }

    @Override
    public Job getJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().map(e -> e.copy()).orElse(null);
    }

    @Override
    public void updateJobMetaInfo(String jobId, String jobName, String jobScheduledTime, Long jobTimeout, String description) {
        if (jobId == null || jobId.isEmpty())
            return;

        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> {
            e.setJobName(jobName);
            e.setJobScheduledTime(jobScheduledTime);
            e.setJobTimeout(jobTimeout);
            e.setDescription(description);
        });
    }

    @Override
    public void updateJobRuntimeInfoByJobId(String jobId, String latestTaskId, Long latestTaskRunTime, Task.TaskStatusEnum latestTaskStatus) {
        if (jobId == null || jobId.isEmpty())
            return;

        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> {
            e.setLatestTaskId(latestTaskId);
            e.setLatestTaskRunTime(latestTaskRunTime);
            e.setLatestTaskStatus(latestTaskStatus);
        });

//        Job job = getJobByJobId_internal(jobId);
//        if (job != null) {
//            job.setLatestTaskId(latestTaskId);
//            job.setLatestTaskRunTime(latestTaskRunTime);
//            job.setLatestTaskStatus(latestTaskStatus);
//        }
    }

    @Override
    public void updateJobLatestTaskStatusByTaskId(String latestTaskId, Task.TaskStatusEnum latestTaskStatus) {
        if (latestTaskId == null || latestTaskId.isEmpty())
            return;

        Job job = getJobByTaskId_internal(latestTaskId);

        if (job == null || !latestTaskId.equals(job.getLatestTaskId()))
            return;

        job.setLatestTaskStatus(latestTaskStatus);
    }

    private Job getJobByJobId_internal(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().orElse(null);
    }

    @Override
    public Job getJobByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        String jobId = taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().map(e -> e.getJobId()).orElse(null);

        if (jobId == null || jobId.isEmpty())
            return null;

        Job job = jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().orElse(null);

        return job;
    }

    private Task.TaskStatusEnum mapTaskResultToTaskStatus(TaskResult.TaskResultEnum tre) {
        Task.TaskStatusEnum status;
        switch (tre) {
            case SUCCESS:
                status = Task.TaskStatusEnum.COMPLETED_SUCCESS;
                break;
            case SUCCESS_WITHWARNING:
                status = Task.TaskStatusEnum.COMPLETE_SUCCESSWITHWARNING;
                break;
            case FAILED:
                status = Task.TaskStatusEnum.COMPLETE_FAILED;
                break;
            default:
                throw new RuntimeException("Unrecognized result");
        }
        return status;
    }

    @Override
    public void handleTaskResult(TaskResult tr) {
        if (tr == null || tr.getTaskId() == null || tr.getTaskId().isEmpty())
            return;

        // update task
        String taskId = tr.getTaskId();

        Task task = getTaskByTaskId_internal(taskId);
        if (task != null && Task.inRunning(task.getTaskStatus())) { // task still in running (not timeout, cancelled)
            task.setTaskStatus(mapTaskResultToTaskStatus(tr.getResult()));
            task.setEndTime(tr.getEndTime());
            task.setResultInfo(tr.getResultInfo());
        }

        // update job
        Job job = getJobByTaskId_internal(taskId);

        if (job != null && job.getLatestTaskId() != null && job.getLatestTaskId().equals(taskId)) {
            if (Task.inRunning(job.getLatestTaskStatus())) { // job still in running
                job.setLatestTaskStatus(mapTaskResultToTaskStatus(tr.getResult()));
            }
        }
    }

    private Job getJobByTaskId_internal(String taskId) {
        Task task = getTaskByTaskId_internal(taskId);
        if (task == null || task.getJobId() == null || task.getJobId().isEmpty())
            return null;

        return jobList.parallelStream().filter(e -> task.getJobId().equals(e.getJobId())).findAny().orElse(null);
    }

    private Task getTaskByTaskId_internal(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().orElse(null);
    }

    @Override
    public void deleteJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        Job job = getJobByJobId_internal(jobId);
        if (job != null && Task.inRunning(job.getLatestTaskStatus())) {
            //
            job.setLatestTaskStatus(Task.TaskStatusEnum.CANCELLED);
        }

        List<Task> taskList = getTaskListByJobId(jobId);
        if (taskList != null) {
            taskList.parallelStream().filter(e -> Task.inRunning(e.getTaskStatus()))
                    .forEach(e -> {
                        cancelTaskByTaskId(e.getTaskId());

                        e.setTaskStatus(Task.TaskStatusEnum.CANCELLED);
                        e.setEndTime(System.currentTimeMillis());
                    });
        }

        doDeleteTaskByJobId(jobId);

        doDeleteJobByJobId(jobId);
    }

    @Override
    public void cancelTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return;

        // cancel running task
        executor.cancelTaskByTaskId(taskId);

        // update job
        Job job = getJobByTaskId_internal(taskId);

        if (job == null) // not found
            return;

        job.setLatestTaskStatus(Task.TaskStatusEnum.CANCELLED);

        // update task
        taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().ifPresent(e -> {
            e.setTaskStatus(Task.TaskStatusEnum.CANCELLED);
            e.setEndTime(System.currentTimeMillis());
        });
    }

    private void doDeleteTaskByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        taskList.parallelStream().filter(e -> jobId.equals(e.getJobId())).forEach(e -> taskList.remove(e));
    }

    private void doDeleteJobByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        jobList.parallelStream().filter(e -> jobId.equals(e.getJobId())).findAny().ifPresent(e -> taskList.remove(e));
    }

    @Override
    public void createTaskByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        Task task = new Task(generateUUID(),
                jobId,
                "This is task name",
                111L,
                "This is task description",
                System.currentTimeMillis(),
                null,
                Task.TaskStatusEnum.READY,
                null);

        taskList.add(task);

        Job job = getJobByTaskId_internal(task.getTaskId());

        if (job != null) {
            job.setLatestTaskId(task.getTaskId());
            job.setLatestTaskRunTime(task.getStartTime());
            job.setLatestTaskStatus(task.getTaskStatus());
        }
    }

    private List<Task> getTaskList_internal() {
        return taskList;
    }

    @Override
    public List<Task> getTaskList() {
        return taskList.parallelStream().map(e -> e.copy())
                .sorted(Comparator.comparing(Task::getStartTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public List<Task> getTaskListByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> jobId.equals(e.getJobId())).map(e -> e.copy())
                .sorted(Comparator.comparing(Task::getStartTime)).collect(Collectors.toList());
    }

    @Override
    public Task getTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return null;

        return taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny()
                .map(e -> e.copy()).orElse(null);
    }

    @Override
    public void deleteTaskByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty())
            return;

        taskList.parallelStream().filter(e -> taskId.equals(e.getTaskId())).findAny().ifPresent(e -> taskList.remove(e));
    }

    @Override
    public void deleteTaskByJobId(String jobId) {
        if (jobId == null || jobId.isEmpty())
            return;

        taskList.parallelStream().filter(e -> jobId.equals(e.getJobId())).forEach(e -> taskList.remove(e));
    }
}
