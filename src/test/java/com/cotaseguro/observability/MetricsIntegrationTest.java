package com.cotaseguro.observability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cotaseguro.domain.Role;
import com.cotaseguro.domain.User;
import com.cotaseguro.dto.LoginRequest;
import com.cotaseguro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
class MetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        saveUser("admin", "admin123", Role.ADMIN);
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void infoEndpointIsPublicAndExposesAppName() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("CotaSeguro")));
    }

    @Test
    void metricsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exposesCustomQuotesGeneratedMetric() throws Exception {
        mockMvc.perform(get("/actuator/metrics/{name}", "cotaseguro.quotes.generated")
                        .header("Authorization", bearer("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("cotaseguro.quotes.generated")));
    }

    @Test
    void exposesCustomPoliciesIssuedMetric() throws Exception {
        mockMvc.perform(get("/actuator/metrics/{name}", "cotaseguro.policies.issued")
                        .header("Authorization", bearer("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("cotaseguro.policies.issued")));
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
