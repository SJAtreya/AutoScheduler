package com.scheduler.booking

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
class AutoSchedulerApplication {

	static void main(String[] args) {
		SpringApplication.run AutoSchedulerApplication, args
	}
}
