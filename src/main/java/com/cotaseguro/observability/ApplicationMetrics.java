package com.cotaseguro.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMetrics {

    private final Counter quotesGenerated;
    private final Counter policiesIssued;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.quotesGenerated = Counter.builder("cotaseguro.quotes.generated")
                .description("Number of quotes generated")
                .register(meterRegistry);
        this.policiesIssued = Counter.builder("cotaseguro.policies.issued")
                .description("Number of policies issued")
                .register(meterRegistry);
    }

    public void quoteGenerated() {
        quotesGenerated.increment();
    }

    public void policyIssued() {
        policiesIssued.increment();
    }

}
