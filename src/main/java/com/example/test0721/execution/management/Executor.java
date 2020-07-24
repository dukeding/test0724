package com.example.test0721.execution.management;

public interface Executor {
    public boolean startNewTaskByTaskId(String taskId, Long timeout, Object otherInfo);

    public void cancelTaskByTaskId(String taskId);
}
