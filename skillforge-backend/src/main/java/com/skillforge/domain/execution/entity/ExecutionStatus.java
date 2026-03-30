package com.skillforge.domain.execution.entity;

public enum ExecutionStatus {
    QUEUED,
    RUNNING,
    SUCCESS,
    TLE,
    MEMORY_LIMIT_EXCEEDED,
    RUNTIME_ERROR,
    COMPILATION_ERROR,
    FAILED
}
