package com.cotaseguro.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PolicyIssuancePublisher {

    private final RabbitTemplate rabbitTemplate;

    public PolicyIssuancePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(PolicyIssuanceMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.POLICY_EXCHANGE, RabbitConfig.POLICY_ISSUANCE_ROUTING_KEY, message);
    }

}
