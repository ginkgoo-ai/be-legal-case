package com.ginkgooai.legalcase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for event handling
 */
@Configuration
@EnableAsync
@EnableTransactionManagement
public class EventConfig {

	// Configuration for event handling
	// The annotations enable async processing and transaction management
	// which are critical for event-driven architecture

}