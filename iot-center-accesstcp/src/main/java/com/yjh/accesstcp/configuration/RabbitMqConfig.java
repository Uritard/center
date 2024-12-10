package com.yjh.accesstcp.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zyy
 */
@Configuration
public class RabbitMqConfig {
    /**
     * 定义队列名称
     */
    public static final String QUEUE_NAME = "yjhQueueInspection";
    /**
     * 定义交换器名称
     */
    public static final String EXCHANGE_NAME = "yjhExchangeInspection";
    /**
     * 定义路由键
     */
    public static final String ROUTING_KEY = "yjh_inspection_result";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
