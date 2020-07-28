package com.example.test0721.execution.management;

public interface Executor {
    // Executor doesn't use jobId. It is just used for callback
    public boolean startNewTaskByTaskId(String taskId, String jobId, Long timeout, Object otherInfo);

    public void cancelTaskByTaskId(String taskId);
}
