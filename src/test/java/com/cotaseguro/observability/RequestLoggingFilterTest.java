package com.cotaseguro.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void generatesRequestIdHeaderAndClearsContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(REQUEST_ID_HEADER)).isNotBlank();
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void reusesProvidedRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers");
        request.addHeader(REQUEST_ID_HEADER, "external-request-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(REQUEST_ID_HEADER)).isEqualTo("external-request-id");
    }

}
