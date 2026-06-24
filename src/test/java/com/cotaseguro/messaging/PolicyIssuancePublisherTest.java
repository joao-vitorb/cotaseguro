package com.cotaseguro.messaging;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class PolicyIssuancePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PolicyIssuancePublisher publisher;

    @Test
    void publishesToConfiguredExchangeAndRoutingKey() {
        PolicyIssuanceMessage message = new PolicyIssuanceMessage(7L);

        publisher.publish(message);

        verify(rabbitTemplate).convertAndSend(
                RabbitConfig.POLICY_EXCHANGE, RabbitConfig.POLICY_ISSUANCE_ROUTING_KEY, message);
    }

}
