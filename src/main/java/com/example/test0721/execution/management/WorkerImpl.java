package com.example.test0721.execution.management;

import com.example.test0721.pojo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerImpl implements Worker{
    Logger logger = LoggerFactory.getLogger(WorkerImpl.class);

    @Override
    public TaskResult work(String taskId, Object otherInfo) {
        logger.info("Task execution started. Task ID: " + taskId);

        TaskResult taskResult = new TaskResult();

        taskResult.setTaskId(taskId);

        int duration = (int) (Math.random() * 100 * 1000); // 0 -- 99 seconds
        logger.info("Task " + taskId + " would last for " + duration + " milliseconds");
        try {
            Thread.sleep(duration); // mock the task process. Note that it can be interrupted

        } catch (InterruptedException e) {
            logger.info("Task execution interrupted. Task ID: " + taskId);

            // todo: work cleanup

            taskResult.setResult(TaskResult.TaskResultEnum.INTERRUPTED);
            taskResult.setEndTime(System.currentTimeMillis());
            taskResult.setResultInfo("No result. Task execution interrupted. taskId: " + taskId + ", taskResult: " + taskResult);

            return taskResult;
        }

        // generate test result
        TaskResult.TaskResultEnum result = null;
        switch ((int) Math.random() * 3) {
            case 0:
                result = TaskResult.TaskResultEnum.SUCCESS;
                break;
            case 1:
                result = TaskResult.TaskResultEnum.SUCCESS_WITHWARNING;
                break;
            case 2:
                result = TaskResult.TaskResultEnum.FAILED;
                break;
            default:
                throw new RuntimeException("Unrecognized task result");
        }

        taskResult.setResult(result);
        taskResult.setEndTime(System.currentTimeMillis());
        taskResult.setResultInfo("This is task result info");

        logger.info("Task execution finished. Task ID: " + taskId + ", taskResult: " + taskResult);

        return taskResult;
    }
}
