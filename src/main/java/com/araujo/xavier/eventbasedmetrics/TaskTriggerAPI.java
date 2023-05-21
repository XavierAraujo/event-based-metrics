package com.araujo.xavier.eventbasedmetrics;

import akka.actor.typed.ActorSystem;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskEvent;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskTrigger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskTriggerAPI {

    private final ActorSystem<TaskEvent> actorSystem;
    public TaskTriggerAPI(ActorSystem<TaskEvent> actorSystem) {
        this.actorSystem = actorSystem;
    }

    @PostMapping("/triggerTask")
    void triggerTask() {
        actorSystem.tell(new TaskTrigger());
    }

}
