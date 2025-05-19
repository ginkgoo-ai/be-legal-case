package com.ginkgooai.legalcase.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<String> springSecurityAuditorAware() {
		return new SpringSecurityAuditorAware();
	}

}