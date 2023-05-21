package com.araujo.xavier.eventbasedmetrics.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class CompletedTaskEvent extends ApplicationEvent {

    private final UUID taskId;

    public CompletedTaskEvent(Object source, UUID taskId) {
        super(source);
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
