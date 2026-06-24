package com.cotaseguro.policy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.InsuranceType;
import com.cotaseguro.domain.Quote;
import com.cotaseguro.domain.QuoteStatus;
import com.cotaseguro.domain.Role;
import com.cotaseguro.domain.User;
import com.cotaseguro.dto.LoginRequest;
import com.cotaseguro.dto.PolicyIssueRequest;
import com.cotaseguro.repository.CustomerRepository;
import com.cotaseguro.repository.PolicyRepository;
import com.cotaseguro.repository.QuoteRepository;
import com.cotaseguro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        quoteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        saveUser("admin", "admin123", Role.ADMIN);
        saveUser("member", "member123", Role.USER);
        customer = saveCustomer();
    }

    @Test
    void issuePolicyFromApprovedQuoteReturnsCreated() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();

        mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.quoteId").value((int) quoteId));
    }

    @Test
    void issuePolicyAsMemberReturnsForbidden() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();

        mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", bearer("member", "member123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void issuePolicyWithoutTokenReturnsUnauthorized() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void issuePolicyFromPendingQuoteReturnsConflict() throws Exception {
        long quoteId = saveQuote(QuoteStatus.PENDING).getId();

        mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isConflict());
    }

    @Test
    void issuePolicyForUnknownQuoteReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(999999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void issuePolicyTwiceForSameQuoteReturnsConflict() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();
        String adminToken = bearer("admin", "admin123");
        issuePolicy(adminToken, quoteId);

        mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelPolicyAsAdminReturnsCancelled() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();
        String adminToken = bearer("admin", "admin123");
        long policyId = issuePolicy(adminToken, quoteId);

        mockMvc.perform(post("/api/v1/policies/{id}/cancel", policyId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelAlreadyCancelledPolicyReturnsConflict() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();
        String adminToken = bearer("admin", "admin123");
        long policyId = issuePolicy(adminToken, quoteId);

        mockMvc.perform(post("/api/v1/policies/{id}/cancel", policyId).header("Authorization", adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/policies/{id}/cancel", policyId).header("Authorization", adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void listPoliciesByStatusReturnsOk() throws Exception {
        long quoteId = saveQuote(QuoteStatus.APPROVED).getId();
        String adminToken = bearer("admin", "admin123");
        issuePolicy(adminToken, quoteId);

        mockMvc.perform(get("/api/v1/policies")
                        .param("status", "ACTIVE")
                        .header("Authorization", bearer("member", "member123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private long issuePolicy(String bearerToken, long quoteId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/policies")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PolicyIssueRequest(quoteId))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bearer(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        return "Bearer " + token;
    }

    private Quote saveQuote(QuoteStatus status) {
        Quote quote = new Quote();
        quote.setCustomer(customer);
        quote.setInsuranceType(InsuranceType.AUTO);
        quote.setCoverageAmount(new BigDecimal("50000.00"));
        quote.setPremium(new BigDecimal("2500.00"));
        quote.setStatus(status);
        return quoteRepository.save(quote);
    }

    private Customer saveCustomer() {
        Customer newCustomer = new Customer();
        newCustomer.setName("Henry Ford");
        newCustomer.setDocument("88888888888");
        newCustomer.setBirthDate(LocalDate.now().minusYears(40));
        newCustomer.setEmail("henry.ford@example.com");
        return customerRepository.save(newCustomer);
    }

    private void saveUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

}
