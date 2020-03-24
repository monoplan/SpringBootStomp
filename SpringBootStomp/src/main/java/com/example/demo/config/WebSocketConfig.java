package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.demo.common.HttpHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	// prefix 설정
	@Override 
	public void configureMessageBroker(MessageBrokerRegistry registry) { 
		registry.enableSimpleBroker("/subscribe","/queue"); 
		registry.setApplicationDestinationPrefixes("/app"); 
	}
	
	// EndPoint 설정
	@Override 
	public void registerStompEndpoints(StompEndpointRegistry registry) { 
		registry.addEndpoint("/endpoint")
				.addInterceptors(new HttpHandshakeInterceptor())
				.withSockJS(); 
	}

}
