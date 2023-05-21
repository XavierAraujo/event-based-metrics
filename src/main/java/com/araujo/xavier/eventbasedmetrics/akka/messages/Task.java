package com.araujo.xavier.eventbasedmetrics.akka.messages;

import akka.actor.typed.ActorRef;

import java.util.Set;
import java.util.UUID;

public record Task(UUID taskId, Set<String> partitions, ActorRef<TaskEvent> replyTo) implements TaskEvent {
}
