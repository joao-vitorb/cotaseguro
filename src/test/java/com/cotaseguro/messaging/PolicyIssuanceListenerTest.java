package com.cotaseguro.messaging;

import static org.mockito.Mockito.verify;

import com.cotaseguro.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyIssuanceListenerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyIssuanceListener listener;

    @Test
    void delegatesIssuanceToPolicyService() {
        listener.onPolicyIssuanceRequested(new PolicyIssuanceMessage(7L));

        verify(policyService).issue(7L);
    }

}
