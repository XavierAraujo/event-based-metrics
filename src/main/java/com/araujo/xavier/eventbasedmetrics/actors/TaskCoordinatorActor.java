package com.araujo.xavier.eventbasedmetrics.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.araujo.xavier.eventbasedmetrics.akka.messages.Task;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskCompleted;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskEvent;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartition;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartitionResult;
import com.araujo.xavier.eventbasedmetrics.events.ProcessedPartitionTaskResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TaskCoordinatorActor extends AbstractBehavior<TaskEvent> {

    public final static String NAME = "TaskCoordinatorActor";
    private final Logger logger = LoggerFactory.getLogger(TaskCoordinatorActor.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Task task;
    private final Set<String> missingTaskPartitions;

    private TaskCoordinatorActor(ActorContext<TaskEvent> context, ApplicationEventPublisher applicationEventPublisher, Task task) {
        super(context);
        this.applicationEventPublisher = applicationEventPublisher;
        this.task = task;
        this.missingTaskPartitions = new HashSet<>(task.partitions());
        splitTaskByWorkers();
    }
    public static Behavior<TaskEvent> create(ApplicationEventPublisher applicationEventPublisher, Task task) {
        return Behaviors.setup(context -> new TaskCoordinatorActor(context, applicationEventPublisher, task));
    }

    @Override
    public Receive<TaskEvent> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskPartitionResult.class, this::onTaskPartitionResult)
                .build();
    }

    private void splitTaskByWorkers() {
        logger.info("[" + getContext().getSelf() + "]" + " received new task to process: " + task);
        for (String partition : task.partitions()) {
            var taskWorkerActor = getContext().spawn(TaskWorkerActor.create(),
                    String.format("%s-%s", TaskWorkerActor.NAME, UUID.randomUUID()));
            taskWorkerActor.tell(new TaskPartition(task.taskId(), partition, getContext().getSelf()));
        }
    }

    private Behavior<TaskEvent> onTaskPartitionResult(TaskPartitionResult taskPartitionResult) {
        applicationEventPublisher.publishEvent(new ProcessedPartitionTaskResultEvent(
                this,
                taskPartitionResult.taskId(),
                taskPartitionResult.partition()
        ));

        task.replyTo().tell(taskPartitionResult);
        missingTaskPartitions.remove(taskPartitionResult.partition());
        if (missingTaskPartitions.isEmpty()) {
            task.replyTo().tell(new TaskCompleted(task.taskId()));
            return Behaviors.stopped();
        }
        return Behaviors.same();
    }
}
