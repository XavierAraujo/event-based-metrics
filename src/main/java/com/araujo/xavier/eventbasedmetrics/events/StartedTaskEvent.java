package com.araujo.xavier.eventbasedmetrics.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class StartedTaskEvent extends ApplicationEvent {

    private final UUID taskId;

    public StartedTaskEvent(Object source, UUID taskId) {
        super(source);
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
