package com.araujo.xavier.eventbasedmetrics.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartition;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskPartitionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskWorkerActor extends AbstractBehavior<TaskPartition> {

    public final static String NAME = "TaskWorkerActor";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = LoggerFactory.getLogger(TaskWorkerActor.class);
    public TaskWorkerActor(ActorContext<TaskPartition> context) {
        super(context);
    }

    public static Behavior<TaskPartition> create() {
        return Behaviors.setup(TaskWorkerActor::new);
    }
    private final Random RANDOM = new Random();

    @Override
    public Receive<TaskPartition> createReceive() {
        return newReceiveBuilder()
                .onMessage(TaskPartition.class, this::onTaskPartition)
                .build();
    }

    private Behavior<TaskPartition> onTaskPartition(TaskPartition taskPartition) {
        logger.info("[" + getContext().getSelf() + "]" + " received task partition: " + taskPartition);
        executorService.schedule(
                () -> taskPartition.replyTo().tell(getDummyTaskPartitionResult(taskPartition)),
                RANDOM.nextInt(2000),
                TimeUnit.MILLISECONDS);
        return Behaviors.stopped();
    }

    private TaskPartitionResult getDummyTaskPartitionResult(TaskPartition taskPartition) {
        return new TaskPartitionResult(
                taskPartition.taskId(),
                taskPartition.partition(),
                IntStream.range(0, RANDOM.nextInt(10)).mapToObj(i -> UUID.randomUUID()).collect(Collectors.toList()));
    }
}
