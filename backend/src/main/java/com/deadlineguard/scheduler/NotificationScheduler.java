package com.deadlineguard.scheduler;

import com.deadlineguard.config.NotificationSchedulerConfig;
import com.deadlineguard.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background tasks driving automated deadline countdown alerts,
 * overdue checks, and daily student academic digests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final NotificationSchedulerConfig schedulerConfig;

    /**
     * Periodic scan for 48h, 24h, 6h countdowns and overdue tasks.
     * Defaults to hourly, configurable via deadlineguard.notifications.scan-cron.
     */
    @Scheduled(cron = "${deadlineguard.notifications.scan-cron:0 0 * * * *}")
    public void runDeadlineScan() {
        if (!schedulerConfig.isEnabled()) {
            log.debug("Notification scheduler is disabled; skipping deadline scan.");
            return;
        }

        log.info("Running automated deadline reminder background scan...");
        try {
            notificationService.processDeadlineReminders();
        } catch (Exception e) {
            log.error("Error occurred during scheduled deadline reminder scan: ", e);
        }
    }

    /**
     * Daily morning academic summary digest for each registered student.
     * Defaults to 07:30 daily, configurable via deadlineguard.notifications.daily-summary-cron.
     */
    @Scheduled(cron = "${deadlineguard.notifications.daily-summary-cron:0 30 7 * * *}")
    public void runDailySummary() {
        if (!schedulerConfig.isEnabled()) {
            log.debug("Notification scheduler is disabled; skipping daily summary.");
            return;
        }

        log.info("Running automated daily summary notification background job...");
        try {
            notificationService.processDailySummaries();
        } catch (Exception e) {
            log.error("Error occurred during scheduled daily summary notification job: ", e);
        }
    }
}
