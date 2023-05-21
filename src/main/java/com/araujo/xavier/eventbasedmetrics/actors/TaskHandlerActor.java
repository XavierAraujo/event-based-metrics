package com.araujo.xavier.eventbasedmetrics.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.araujo.xavier.eventbasedmetrics.akka.messages.Task;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskCompleted;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartitionResult;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskTrigger;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskEvent;
import com.araujo.xavier.eventbasedmetrics.events.CompletedTaskEvent;
import com.araujo.xavier.eventbasedmetrics.events.StartedTaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskHandlerActor extends AbstractBehavior<TaskEvent> {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Logger logger = LoggerFactory.getLogger(TaskHandlerActor.class);
    private TaskHandlerActor(ActorContext<TaskEvent> context,
                             ApplicationEventPublisher applicationEventPublisher) {
        super(context);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public static Behavior<TaskEvent> create(ApplicationEventPublisher applicationEventPublisher) {
        return Behaviors.setup(context -> new TaskHandlerActor(context, applicationEventPublisher));
    }

    @Override
    public Receive<TaskEvent> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskTrigger.class, this::onTaskTrigger)
                .onMessage(TaskCompleted.class, this::onTaskCompleted)
                .onMessage(TaskPartitionResult.class, this::onTaskPartitionResult)
                .build();
    }

    private Behavior<TaskEvent> onTaskTrigger(TaskTrigger taskTrigger) {
        logger.info("[" + getContext().getSelf() + "]" + " received task trigger ");
        Task task = createDummyTask();
        applicationEventPublisher.publishEvent(new StartedTaskEvent(this, task.taskId()));

        getContext().spawn(TaskCoordinatorActor.create(applicationEventPublisher, task),
                String.format("%s-%s", TaskCoordinatorActor.NAME, UUID.randomUUID()));

        return Behaviors.same();
    }

    private Behavior<TaskEvent> onTaskPartitionResult(TaskPartitionResult taskPartitionResult) {
        logger.info("[" + getContext().getSelf() + "]" + " received task partition result: " + taskPartitionResult);
        var taskPublisherActor = getContext().spawn(TaskPublisherActor.create(applicationEventPublisher),
                String.format("%s-%s", TaskPublisherActor.NAME, UUID.randomUUID()));
        taskPublisherActor.tell(taskPartitionResult);
        return Behaviors.same();
    }

    private Behavior<TaskEvent> onTaskCompleted(TaskCompleted taskCompleted) {
        logger.info("[" + getContext().getSelf() + "]" + " received task completed: " + taskCompleted);
        applicationEventPublisher.publishEvent(new CompletedTaskEvent(this, taskCompleted.taskId()));
        return Behaviors.same();
    }

    private Task createDummyTask() {
        return new Task(
                UUID.randomUUID(),
                IntStream.range(0, 10)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toSet()),
                getContext().getSelf()
        );
    }
}
