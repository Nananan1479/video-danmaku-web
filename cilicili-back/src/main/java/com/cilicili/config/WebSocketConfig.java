package com.cilicili.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 *
 * @author Nananan1479
 * @date 2026/6/2 15:46
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理（消息路由规则）<br>
     * 定义客户端订阅消息和发送消息的地址前缀。,此处只做单向传递（服务端->客户端），确保前端传来的信息携带JWT-Token
     *
     * @param registry
     *
     * @author Nananan1479
     * @date 2026/6/2 15:54

     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅的前缀：如 /topic/danmaku/{videoId}
        registry.enableSimpleBroker("/topic");
        // 客户端发送消息到服务器的前缀（我们不使用，而是用 HTTP 发送）
        // registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket 握手入口<br>
     * 配置 WebSocket 的握手地址，并决定是否支持降级方案。
     *
     * @param registry
     *
     * @author Nananan1479
     * @date 2026/6/2 15:48

     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")   // 允许跨域
                .withSockJS();                   // 兼容不支持 WebSocket 的浏览器
    }
}
