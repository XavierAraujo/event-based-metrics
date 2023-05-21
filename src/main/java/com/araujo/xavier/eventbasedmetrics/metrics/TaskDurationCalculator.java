package com.araujo.xavier.eventbasedmetrics.metrics;

import com.araujo.xavier.eventbasedmetrics.events.CompletedTaskEvent;
import com.araujo.xavier.eventbasedmetrics.events.PublishedPartitionTaskResultEvent;
import com.araujo.xavier.eventbasedmetrics.events.ProcessedPartitionTaskResultEvent;
import com.araujo.xavier.eventbasedmetrics.events.StartedTaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TaskDurationCalculator implements ApplicationListener<ApplicationEvent> {

    private static class TaskManagement {
        Timestamp startedAt = Timestamp.from(Instant.now());
        Set<String> missingPublishing = new HashSet<>();
        boolean isCompleted = false;
    }
    private final Logger logger = LoggerFactory.getLogger(TaskDurationCalculator.class);

    private final Map<UUID, TaskManagement> tasks = new HashMap<>();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        switch (event) {
            case StartedTaskEvent e -> handleStartedTaskEvent(e);
            case ProcessedPartitionTaskResultEvent e -> handleProcessedPartitionTaskResultEvent(e);
            case PublishedPartitionTaskResultEvent e -> handlePublishedPartitionTaskResultEvent(e);
            case CompletedTaskEvent e -> handleCompletedTaskEvent(e);
            default -> {}
        }
    }

    private void handleStartedTaskEvent(StartedTaskEvent event) {
        tasks.put(event.getTaskId(), new TaskManagement());
    }

    private void handleProcessedPartitionTaskResultEvent(ProcessedPartitionTaskResultEvent event) {
        var taskManagement = tasks.get(event.getTaskId());
        if (taskManagement == null) {
            logger.error("Received a ProcessedPartitionTaskResultEvent for an invalid task ID");
            return;
        }

        taskManagement.missingPublishing.add(event.getPartition());
    }

    private void handlePublishedPartitionTaskResultEvent(PublishedPartitionTaskResultEvent event) {
        var taskManagement = tasks.get(event.getTaskId());
        if (taskManagement == null) {
            logger.error("Received a PublishedPartitionTaskResultEvent for an invalid task ID");
            return;
        }

        taskManagement.missingPublishing.remove(event.getPartition());
        if (isTaskCompletedAndPublished(taskManagement)) {
            publishTaskDurationAndCleanup(event.getTaskId());
        }
    }

    private void handleCompletedTaskEvent(CompletedTaskEvent event) {
        var taskManagement = tasks.get(event.getTaskId());
        if (taskManagement == null) {
            logger.error("Received a CompletedTaskEvent for an invalid task ID");
            return;
        }

        taskManagement.isCompleted = true;
        if (isTaskCompletedAndPublished(taskManagement)) {
            publishTaskDurationAndCleanup(event.getTaskId());
        }
    }

    private boolean isTaskCompletedAndPublished(TaskManagement taskManagement) {
        return taskManagement.isCompleted && taskManagement.missingPublishing.isEmpty();
    }

    private void publishTaskDurationAndCleanup(UUID taskId) {
        var taskManagement = tasks.get(taskId);
        var now = Timestamp.from(Instant.now());
        long duration = now.getTime() - taskManagement.startedAt.getTime();
        logger.info("Task with ID " + taskId.toString() + " took " + duration + " milliseconds to process");
        tasks.remove(taskId);
    }
}
