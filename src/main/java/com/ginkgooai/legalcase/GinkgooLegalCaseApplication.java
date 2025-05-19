package com.ginkgooai.legalcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableFeignClients
public class GinkgooLegalCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GinkgooLegalCaseApplication.class, args);
	}

}
