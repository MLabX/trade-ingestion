package com.magiccode.tradeingestion.integration.solace;

import com.magiccode.tradeingestion.config.SolaceContainerConfig;
import com.magiccode.tradeingestion.config.SolaceContainerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Import(SolaceContainerConfig.class)
public class SolaceContainerTest {

    @Autowired
    private SolaceContainerManager solaceManager;

    @BeforeAll
    static void setUp() {
        // Container is now managed by Spring
    }

    @AfterAll
    static void tearDown() {
        // Container is now managed by Spring
    }

    @Test
    void testSolaceContainerIsRunning() {
        String host = solaceManager.getContainerHost();
        int port = solaceManager.getContainerPort(8080);
        
        // Check if SEMP API is accessible
        try {
            URL url = new URL("http://" + host + ":" + port + "/SEMP");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4="); // admin:admin in base64
            
            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode, "SEMP API should be accessible");
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to SEMP API", e);
        }
    }

    @Test
    void testQueuesAreConfigured() {
        String host = solaceManager.getContainerHost();
        int port = solaceManager.getContainerPort(8080);
        
        // Check if required queues exist
        String[] queues = {"DEAL.DLQ", "DEAL.IN", "DEAL.OUT"};
        for (String queue : queues) {
            try {
                URL url = new URL("http://" + host + ":" + port + "/SEMP/v2/config/msgVpns/default/queues/" + queue);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
                
                int responseCode = connection.getResponseCode();
                assertEquals(200, responseCode, "Queue " + queue + " should exist");
                
                // Read response to verify queue properties
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    assertTrue(response.toString().contains("\"queueName\":\"" + queue + "\""), 
                              "Queue " + queue + " should have correct name");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to check queue " + queue, e);
            }
        }
    }

    @Test
    void testSubscriptionIsConfigured() {
        String host = solaceManager.getContainerHost();
        int port = solaceManager.getContainerPort(8080);
        
        // Check if subscription exists
        try {
            URL url = new URL("http://" + host + ":" + port + "/SEMP/v2/config/msgVpns/default/queues/DEAL.IN/subscriptions/DEAL.IN");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
            
            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode, "Subscription should exist");
            
            // Read response to verify subscription properties
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                assertTrue(response.toString().contains("\"subscriptionTopic\":\"DEAL.IN\""), 
                          "Subscription should have correct topic");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check subscription", e);
        }
    }
} 