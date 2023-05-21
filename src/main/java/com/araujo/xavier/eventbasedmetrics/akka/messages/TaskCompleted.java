package com.araujo.xavier.eventbasedmetrics.akka.messages;

import java.util.UUID;

public record TaskCompleted(UUID taskId) implements TaskEvent {
}
