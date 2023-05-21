package com.araujo.xavier.eventbasedmetrics.akka.messages;

import akka.actor.typed.ActorRef;

import java.util.UUID;

public record TaskPartition(UUID taskId, String partition, ActorRef<TaskEvent> replyTo) implements TaskEvent {
}
