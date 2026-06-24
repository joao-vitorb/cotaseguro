package com.cotaseguro.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String POLICY_EXCHANGE = "cotaseguro.policy";
    public static final String POLICY_ISSUANCE_QUEUE = "cotaseguro.policy.issuance";
    public static final String POLICY_ISSUANCE_ROUTING_KEY = "policy.issuance.requested";

    @Bean
    public DirectExchange policyExchange() {
        return new DirectExchange(POLICY_EXCHANGE);
    }

    @Bean
    public Queue policyIssuanceQueue() {
        return QueueBuilder.durable(POLICY_ISSUANCE_QUEUE).build();
    }

    @Bean
    public Binding policyIssuanceBinding(Queue policyIssuanceQueue, DirectExchange policyExchange) {
        return BindingBuilder.bind(policyIssuanceQueue).to(policyExchange).with(POLICY_ISSUANCE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
