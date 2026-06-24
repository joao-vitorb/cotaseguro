package com.cotaseguro.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cotaseguro.domain.Role;
import com.cotaseguro.domain.User;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.dto.LoginRequest;
import com.cotaseguro.repository.CustomerRepository;
import com.cotaseguro.repository.PolicyRepository;
import com.cotaseguro.repository.QuoteRepository;
import com.cotaseguro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CustomerControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        quoteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        saveUser("admin", "admin123", Role.ADMIN);
        saveUser("member", "member123", Role.USER);
    }

    @Test
    void createCustomerAsAdminReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Alice Martins"));
    }

    @Test
    void createCustomerAsMemberReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer("member", "member123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCustomerWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCustomerWithInvalidBodyReturnsBadRequest() throws Exception {
        CustomerRequest invalid = new CustomerRequest("", "abc", LocalDate.of(1990, 4, 12), "not-an-email");

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomerWithDuplicateDocumentReturnsConflict() throws Exception {
        String token = bearer("admin", "admin123");
        createCustomer(token, sampleRequest());

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void listCustomersAsMemberReturnsOk() throws Exception {
        createCustomer(bearer("admin", "admin123"), sampleRequest());

        mockMvc.perform(get("/api/v1/customers")
                        .header("Authorization", bearer("member", "member123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getCustomerByIdReturnsCustomer() throws Exception {
        String token = bearer("admin", "admin123");
        long id = createCustomer(token, sampleRequest());

        mockMvc.perform(get("/api/v1/customers/{id}", id)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id));
    }

    @Test
    void getUnknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{id}", 999999)
                        .header("Authorization", bearer("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomerAsAdminReturnsNoContent() throws Exception {
        String token = bearer("admin", "admin123");
        long id = createCustomer(token, sampleRequest());

        mockMvc.perform(delete("/api/v1/customers/{id}", id)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/customers/{id}", id)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    private CustomerRequest sampleRequest() {
        return new CustomerRequest("Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");
    }

    private long createCustomer(String bearerToken, CustomerRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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

    private void saveUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

}
