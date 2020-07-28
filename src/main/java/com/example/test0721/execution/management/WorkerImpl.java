package com.example.test0721.execution.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerImpl implements Worker{
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object work(String taskId, Object otherInfo) {
        logger.info("Task execution started. Task ID: " + taskId);

        String workerResult = null;

        int duration = (int) (Math.random() * 100 * 1000); // 0 -- 99 seconds
        logger.info("Task " + taskId + " would last for " + duration + " milliseconds");
        try {
            Thread.sleep(duration); // mock the task process. Note that it can be interrupted

        } catch (InterruptedException e) { // worker decides by itself whether or not to throw InterruptedException
            logger.info("Task execution interrupted. Task ID: " + taskId);

            // worker cleanup here
            workerResult = "This is worker result of interrupted";
            return workerResult;
        }

        workerResult = "This is normal worker result";
        logger.info("Task execution finished. Task ID: " + taskId + ", workerResult: " + workerResult);

        return workerResult;
    }
}
