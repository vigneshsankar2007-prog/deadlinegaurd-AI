package com.deadlineguard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration and scheduler enablement for automated deadline reminders and daily summaries.
 */
@Data
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "deadlineguard.notifications")
public class NotificationSchedulerConfig {

    private boolean enabled = true;
    private String scanCron = "0 0 * * * *";
    private String dailySummaryCron = "0 30 7 * * *";
}
