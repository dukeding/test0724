package com.example.test0721.execution.management;

import com.example.test0721.job.management.JobManager;
import com.example.test0721.job.management.JobScheduler;
import com.example.test0721.pojo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service(value = "Executor")
public class ExecutorImpl implements Executor{
    private static final Long DEFAULT_TIMEOUT = 300 * 1000L; // default timeout

    private Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    @Autowired
    private JobManager jobManager;

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private static Map<String, Future<Void>> futureMap = new HashMap<String, Future<Void>>();

    private void doHandleTaskResult(TaskResult tr) {
        futureMap.remove(tr.getTaskId()); // may already been removed by cancel task

        // notify job manager
        jobManager.handleTaskResult(tr);
    }

    @Override
    public boolean startNewTaskByTaskId(String taskId, Long timeout, Object otherInfo) {
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> (new WorkerImpl()).work(taskId, otherInfo), executorService) // invoke task
                        .thenAccept(e -> doHandleTaskResult(e))
                        .orTimeout(timeout != null ? timeout : DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS); // then handle task result

        futureMap.put(taskId, future); // put the task into map

        return true;
    }

    @Override
    public void cancelTaskByTaskId(String taskId) {
        Future<Void> future = futureMap.get(taskId); // may already been removed by task normal finish
        if (future == null) {
            logger.warn("taskId " + taskId + " not found in taskFutureMap");
            return;
        }

        future.cancel(true); // ignore the cancel result

        futureMap.remove(taskId); // may already been removed by task normal finish
    }

}
