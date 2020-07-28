package com.example.test0721.execution.management;

import com.example.test0721.job.management.JobManager;
import com.example.test0721.pojo.Task;
import com.example.test0721.pojo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service(value = "Executor")
public class ExecutorImpl implements Executor {
    private static final Long DEFAULT_TIMEOUT = 300 * 1000L; // default timeout

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JobManager jobManager;

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private static Map<String, Future<Object>> futureMap = new HashMap<String, Future<Object>>();

    private TaskResult doHandleTaskResult(String taskId, String jobId, Object workerResult, Throwable throwable) {
        futureMap.remove(taskId);

        TaskResult tr = new TaskResult();
        tr.setTaskId(taskId);
        tr.setResultInfo(workerResult);

        if (throwable != null) {
            if (throwable instanceof TimeoutException) {
                tr.setResult(Task.TaskStatusEnum.TIMEOUT);
            } else if (throwable instanceof CancellationException) {
                tr.setResult(Task.TaskStatusEnum.CANCELLED);
            } else if (throwable instanceof InterruptedException) {
                tr.setResult(Task.TaskStatusEnum.INTERRUPTED);
            } else {
                // other exceptional finish
                tr.setResult(Task.TaskStatusEnum.EXCEPTIONAL);
            }
        } else {
            tr.setResult(Task.TaskStatusEnum.COMPLETE_SUCCESS);
        }

        // notify job manager
        jobManager.handleTaskResult(tr, jobId);

        return tr;
    }

    @Override
    public boolean startNewTaskByTaskId(String taskId, String jobId, Long timeout, Object otherInfo) {
        if (taskId == null || taskId.isEmpty())
            return false;

        CompletableFuture<Object> future =
                CompletableFuture.supplyAsync(() -> (new WorkerImpl()).work(taskId, otherInfo), executorService) // invoke task
                        .orTimeout(timeout != null ? timeout : DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS); // then handle task result

        futureMap.put(taskId, future); // put the task into map

        future.handleAsync((workerResult, throwable) -> doHandleTaskResult(taskId, jobId, workerResult, throwable)); // callback function for result

        return true;
    }

    @Override
    public void cancelTaskByTaskId(String taskId) {
        Future<Object> future = futureMap.get(taskId); // may already been removed by task normal finish or timeout
        if (future == null) {
            logger.warn("taskId " + taskId + " not found in taskFutureMap");
            return;
        }

        future.cancel(true); // ignore the cancel result
    }

}
