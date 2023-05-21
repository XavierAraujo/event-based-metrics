package com.araujo.xavier.eventbasedmetrics.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartitionResult;
import com.araujo.xavier.eventbasedmetrics.events.PublishedPartitionTaskResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskPublisherActor extends AbstractBehavior<TaskPartitionResult> {

    public final static String NAME = "TaskPublisherActor";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final Logger logger = LoggerFactory.getLogger(TaskPublisherActor.class);
    private final Random RANDOM = new Random();

    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskPublisherActor(ActorContext<TaskPartitionResult> context,
                              ApplicationEventPublisher applicationEventPublisher) {
        super(context);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public static Behavior<TaskPartitionResult> create(ApplicationEventPublisher applicationEventPublisher) {
        return Behaviors.setup(context -> new TaskPublisherActor(context, applicationEventPublisher));
    }

    @Override
    public Receive<TaskPartitionResult> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskPartitionResult.class, this::onTaskPartitionResult)
                .build();
    }

    private Behavior<TaskPartitionResult> onTaskPartitionResult(TaskPartitionResult taskPartitionResult) {
        logger.info("[" + getContext().getSelf() + "]" + " publishing task partition result: " + taskPartitionResult);
        executorService.schedule(
                () -> {
                    logger.info("[" + getContext().getSelf() + "]" + " published task partition result: " + taskPartitionResult);
                    applicationEventPublisher.publishEvent(new PublishedPartitionTaskResultEvent(
                            this,
                            taskPartitionResult.taskId(),
                            taskPartitionResult.partition()
                    ));
                },
                RANDOM.nextInt(2000),
                TimeUnit.MILLISECONDS);


        return Behaviors.stopped(); // The actor shutdowns after publishing the information
    }
}
