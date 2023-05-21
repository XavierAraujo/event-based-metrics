package com.araujo.xavier.eventbasedmetrics.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ProcessedPartitionTaskResultEvent extends ApplicationEvent {

    private final UUID taskId;

    private final String partition;

    public ProcessedPartitionTaskResultEvent(Object source, UUID taskId, String partition) {
        super(source);
        this.taskId = taskId;
        this.partition = partition;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getPartition() {
        return partition;
    }
}