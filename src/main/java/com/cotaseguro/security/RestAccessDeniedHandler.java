package com.cotaseguro.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final HttpErrorWriter httpErrorWriter;

    public RestAccessDeniedHandler(HttpErrorWriter httpErrorWriter) {
        this.httpErrorWriter = httpErrorWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        httpErrorWriter.write(response, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
    }

}
