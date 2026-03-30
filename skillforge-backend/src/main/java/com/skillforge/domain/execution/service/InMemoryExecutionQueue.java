package com.skillforge.domain.execution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryExecutionQueue implements ExecutionQueue {

    private final LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    @Override
    public void enqueue(Long executionId) {
        queue.offer(executionId);
    }

    @Override
    public List<Long> drain(int maxItems) {
        List<Long> items = new ArrayList<>(Math.max(maxItems, 1));
        queue.drainTo(items, Math.max(maxItems, 1));
        return items;
    }
}
