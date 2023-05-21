package com.araujo.xavier.eventbasedmetrics.akka.messages;

import java.util.List;
import java.util.UUID;

public record TaskPartitionResult(UUID taskId, String partition, List<UUID> validIds) implements TaskEvent {
}
