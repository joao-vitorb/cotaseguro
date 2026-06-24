package com.cotaseguro.messaging;

import com.cotaseguro.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PolicyIssuanceListener {

    private static final Logger log = LoggerFactory.getLogger(PolicyIssuanceListener.class);

    private final PolicyService policyService;

    public PolicyIssuanceListener(PolicyService policyService) {
        this.policyService = policyService;
    }

    @RabbitListener(queues = RabbitConfig.POLICY_ISSUANCE_QUEUE)
    public void onPolicyIssuanceRequested(PolicyIssuanceMessage message) {
        log.info("Processing policy issuance for quote {}", message.quoteId());
        policyService.issue(message.quoteId());
    }

}
