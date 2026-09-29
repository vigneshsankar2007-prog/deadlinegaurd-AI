package com.deadlineguard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Main entry point for DeadlineGuard AI Spring Boot Backend.
 *
 * Architecture:
 * - Layered clean architecture (Controller -> Service -> Repository -> Entity)
 * - Spring Data JPA & Hibernate 6.x
 * - MySQL 8.0+ integration with HikariCP connection pooling
 * - Bean Validation & global exception interception
 */
@Slf4j
@SpringBootApplication
public class DeadlineGuardApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DeadlineGuardApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info("""

                --------------------------------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal:      {}://localhost:{}{}
                \tExternal:   {}://{}:{}{}
                \tProfile(s): {}
                --------------------------------------------------------------------------------""",
                env.getProperty("spring.application.name", "deadlineguard-backend"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? new String[]{"default"} : env.getActiveProfiles()
        );
    }
}
