package com.deadlineguard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and database transaction management configuration.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.deadlineguard.repository")
public class JpaConfig {
}
