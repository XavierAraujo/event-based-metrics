package com.araujo.xavier.eventbasedmetrics;

import akka.actor.typed.ActorSystem;
import com.araujo.xavier.eventbasedmetrics.actors.TaskHandlerActor;
import com.araujo.xavier.eventbasedmetrics.akka.messages.TaskEvent;
import com.araujo.xavier.eventbasedmetrics.metrics.TaskDurationCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBasedMetricsApplicationConfig {

	private final Logger logger = LoggerFactory.getLogger(EventBasedMetricsApplicationConfig.class);
	
	@Bean
	public TaskDurationCalculator taskDurationCalculator() {
		return new TaskDurationCalculator();
	}

	@Bean
	public ActorSystem<TaskEvent> actorSystem(ApplicationEventPublisher applicationEventPublisher) {
		logger.info("Creating actor system");
		// TaskHandlerActor is used as the Akka system guardian actor
		return ActorSystem.create(TaskHandlerActor.create(applicationEventPublisher), "EventBasedMetricsActorSystem");
	}

}
