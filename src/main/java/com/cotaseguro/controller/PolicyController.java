package com.cotaseguro.controller;

import com.cotaseguro.domain.PolicyStatus;
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.dto.PolicyIssueRequest;
import com.cotaseguro.dto.PolicyResponse;
import com.cotaseguro.service.PolicyService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> issue(
            @Valid @RequestBody PolicyIssueRequest request,
            UriComponentsBuilder uriBuilder) {
        PolicyResponse response = policyService.issue(request);
        URI location = uriBuilder.path("/api/v1/policies/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<PolicyResponse> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) PolicyStatus status,
            Pageable pageable) {
        return policyService.list(customerId, status, pageable);
    }

    @GetMapping("/{id}")
    public PolicyResponse getById(@PathVariable Long id) {
        return policyService.getById(id);
    }

    @PostMapping("/{id}/cancel")
    public PolicyResponse cancel(@PathVariable Long id) {
        return policyService.cancel(id);
    }

}
