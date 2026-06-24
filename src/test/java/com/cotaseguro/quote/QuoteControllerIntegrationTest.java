package com.cotaseguro.quote;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.InsuranceType;
import com.cotaseguro.domain.Role;
import com.cotaseguro.domain.User;
import com.cotaseguro.dto.LoginRequest;
import com.cotaseguro.dto.QuoteRequest;
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
class QuoteControllerIntegrationTest {

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

    private Long customerId;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        quoteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        saveUser("admin", "admin123", Role.ADMIN);
        saveUser("member", "member123", Role.USER);
        customerId = saveCustomer().getId();
    }

    @Test
    void createQuoteAsMemberReturnsCreatedWithPremium() throws Exception {
        mockMvc.perform(post("/api/v1/quotes")
                        .header("Authorization", bearer("member", "member123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.premium").value(2500.00));
    }

    @Test
    void createQuoteWithUnknownCustomerReturnsNotFound() throws Exception {
        QuoteRequest request = new QuoteRequest(999999L, InsuranceType.AUTO, new BigDecimal("50000.00"));

        mockMvc.perform(post("/api/v1/quotes")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuoteWithInvalidBodyReturnsBadRequest() throws Exception {
        QuoteRequest request = new QuoteRequest(customerId, InsuranceType.AUTO, new BigDecimal("-1"));

        mockMvc.perform(post("/api/v1/quotes")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQuoteWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void approveQuoteAsAdminReturnsApproved() throws Exception {
        long quoteId = createQuote();

        mockMvc.perform(post("/api/v1/quotes/{id}/approve", quoteId)
                        .header("Authorization", bearer("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveQuoteAsMemberReturnsForbidden() throws Exception {
        long quoteId = createQuote();

        mockMvc.perform(post("/api/v1/quotes/{id}/approve", quoteId)
                        .header("Authorization", bearer("member", "member123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveAlreadyApprovedQuoteReturnsConflict() throws Exception {
        long quoteId = createQuote();
        String adminToken = bearer("admin", "admin123");

        mockMvc.perform(post("/api/v1/quotes/{id}/approve", quoteId).header("Authorization", adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/quotes/{id}/approve", quoteId).header("Authorization", adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void listQuotesByStatusReturnsOk() throws Exception {
        createQuote();

        mockMvc.perform(get("/api/v1/quotes")
                        .param("status", "PENDING")
                        .header("Authorization", bearer("member", "member123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private QuoteRequest sampleRequest() {
        return new QuoteRequest(customerId, InsuranceType.AUTO, new BigDecimal("50000.00"));
    }

    private long createQuote() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/quotes")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
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

    private Customer saveCustomer() {
        Customer customer = new Customer();
        customer.setName("Frank Miller");
        customer.setDocument("66666666666");
        customer.setBirthDate(LocalDate.now().minusYears(30));
        customer.setEmail("frank.miller@example.com");
        return customerRepository.save(customer);
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
