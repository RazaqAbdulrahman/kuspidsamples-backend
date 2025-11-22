package com.kuspidsamples.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Messages with destination prefixed with /topic or /queue will be routed to the broker
        config.enableSimpleBroker("/topic", "/queue");

        // Messages with destination prefixed with /app will be routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the /ws endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Enable SockJS fallback options
    }
}

/**
 * WebSocket Usage Example:
 *
 * To send messages to clients, inject SimpMessagingTemplate in your service/controller:
 *
 * @Autowired
 * private SimpMessagingTemplate messagingTemplate;
 *
 * // Send to all subscribers of a topic
 * messagingTemplate.convertAndSend("/topic/samples", message);
 *
 * // Send to a specific user
 * messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
 *
 *
 * To receive messages from clients, create a controller with @MessageMapping:
 *
 * @Controller
 * public class WebSocketController {
 *
 *     @MessageMapping("/chat")
 *     @SendTo("/topic/messages")
 *     public Message sendMessage(Message message) {
 *         return message;
 *     }
 * }
 */