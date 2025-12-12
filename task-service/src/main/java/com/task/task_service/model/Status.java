package com.task.task_service.model;

public enum Status {
    TODO,
    IN_PROGRESS,
    DONE,
    ARCHIVED;
    public boolean isTerminal(){
        return this == ARCHIVED || this == DONE;
    }
}
