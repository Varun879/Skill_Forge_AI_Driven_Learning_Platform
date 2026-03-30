package com.skillforge.domain.execution.service;

import java.util.List;

public interface ExecutionQueue {
    void enqueue(Long executionId);
    List<Long> drain(int maxItems);
}
