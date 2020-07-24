package com.example.test0721.execution.management;

import com.example.test0721.pojo.TaskResult;

public interface Worker {

    public TaskResult work(String taskId, Object otherInfo);
}
