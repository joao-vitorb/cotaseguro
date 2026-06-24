package com.cotaseguro.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cotaseguro.config.CacheConfig;
import com.cotaseguro.domain.Customer;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.repository.CustomerRepository;
import com.cotaseguro.service.CustomerService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CustomerCacheIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private CustomerRepository customerRepository;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache(CacheConfig.CUSTOMERS_CACHE).clear();
    }

    @Test
    void getByIdCachesResultAndAvoidsSecondLookup() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));

        customerService.getById(1L);
        customerService.getById(1L);

        verify(customerRepository, times(1)).findById(1L);
        assertThat(cacheManager.getCache(CacheConfig.CUSTOMERS_CACHE).get(1L)).isNotNull();
    }

    @Test
    void updateEvictsCachedCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(customerRepository.existsByDocumentAndIdNot(any(), any())).thenReturn(false);
        when(customerRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerService.getById(1L);
        assertThat(cacheManager.getCache(CacheConfig.CUSTOMERS_CACHE).get(1L)).isNotNull();

        customerService.update(1L, request());

        assertThat(cacheManager.getCache(CacheConfig.CUSTOMERS_CACHE).get(1L)).isNull();
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice Martins");
        customer.setDocument("11111111111");
        customer.setBirthDate(LocalDate.of(1990, 4, 12));
        customer.setEmail("alice@example.com");
        return customer;
    }

    private CustomerRequest request() {
        return new CustomerRequest("Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");
    }

}
