package com.app.web.rest;

import com.app.RestangularjsApp;

import com.app.domain.Customer;
import com.app.repository.CustomerRepository;
import com.app.service.CustomerService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CustomerResource REST controller.
 *
 * @see CustomerResource
 */
@RunWith(SpringRunner.class)

@SpringBootTest(classes = RestangularjsApp.class)

public class CustomerResourceIntTest {
    private static final String DEFAULT_CPF = "AAAAA";
    private static final String UPDATED_CPF = "BBBBB";
    private static final String DEFAULT_CNPJ = "AAAAA";
    private static final String UPDATED_CNPJ = "BBBBB";
    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_SECURECOMPANY = "AAAAA";
    private static final String UPDATED_SECURECOMPANY = "BBBBB";
    private static final String DEFAULT_LICENSEPLATE = "AAAAA";
    private static final String UPDATED_LICENSEPLATE = "BBBBB";

    private static final LocalDate DEFAULT_INITIAL_TERM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_INITIAL_TERM = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_FINAL_TERM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FINAL_TERM = LocalDate.now(ZoneId.systemDefault());

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private CustomerService customerService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restCustomerMockMvc;

    private Customer customer;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CustomerResource customerResource = new CustomerResource();
        ReflectionTestUtils.setField(customerResource, "customerService", customerService);
        this.restCustomerMockMvc = MockMvcBuilders.standaloneSetup(customerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Customer createEntity(EntityManager em) {
        Customer customer = new Customer()
                .cpf(DEFAULT_CPF)
                .cnpj(DEFAULT_CNPJ)
                .name(DEFAULT_NAME)
                .securecompany(DEFAULT_SECURECOMPANY)
                .licenseplate(DEFAULT_LICENSEPLATE)
                .initialTerm(DEFAULT_INITIAL_TERM)
                .finalTerm(DEFAULT_FINAL_TERM);
        return customer;
    }

    @Before
    public void initTest() {
        customer = createEntity(em);
    }

    @Test
    @Transactional
    public void createCustomer() throws Exception {
        int databaseSizeBeforeCreate = customerRepository.findAll().size();

        // Create the Customer

        restCustomerMockMvc.perform(post("/api/customers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(customer)))
                .andExpect(status().isCreated());

        // Validate the Customer in the database
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(databaseSizeBeforeCreate + 1);
        Customer testCustomer = customers.get(customers.size() - 1);
        assertThat(testCustomer.getCpf()).isEqualTo(DEFAULT_CPF);
        assertThat(testCustomer.getCnpj()).isEqualTo(DEFAULT_CNPJ);
        assertThat(testCustomer.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCustomer.getSecurecompany()).isEqualTo(DEFAULT_SECURECOMPANY);
        assertThat(testCustomer.getLicenseplate()).isEqualTo(DEFAULT_LICENSEPLATE);
        assertThat(testCustomer.getInitialTerm()).isEqualTo(DEFAULT_INITIAL_TERM);
        assertThat(testCustomer.getFinalTerm()).isEqualTo(DEFAULT_FINAL_TERM);
    }

    @Test
    @Transactional
    public void checkSecurecompanyIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setSecurecompany(null);

        // Create the Customer, which fails.

        restCustomerMockMvc.perform(post("/api/customers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(customer)))
                .andExpect(status().isBadRequest());

        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCustomers() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customers
        restCustomerMockMvc.perform(get("/api/customers?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
                .andExpect(jsonPath("$.[*].cpf").value(hasItem(DEFAULT_CPF.toString())))
                .andExpect(jsonPath("$.[*].cnpj").value(hasItem(DEFAULT_CNPJ.toString())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].securecompany").value(hasItem(DEFAULT_SECURECOMPANY.toString())))
                .andExpect(jsonPath("$.[*].licenseplate").value(hasItem(DEFAULT_LICENSEPLATE.toString())))
                .andExpect(jsonPath("$.[*].initialTerm").value(hasItem(DEFAULT_INITIAL_TERM.toString())))
                .andExpect(jsonPath("$.[*].finalTerm").value(hasItem(DEFAULT_FINAL_TERM.toString())));
    }

    @Test
    @Transactional
    public void getCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get the customer
        restCustomerMockMvc.perform(get("/api/customers/{id}", customer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(customer.getId().intValue()))
            .andExpect(jsonPath("$.cpf").value(DEFAULT_CPF.toString()))
            .andExpect(jsonPath("$.cnpj").value(DEFAULT_CNPJ.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.securecompany").value(DEFAULT_SECURECOMPANY.toString()))
            .andExpect(jsonPath("$.licenseplate").value(DEFAULT_LICENSEPLATE.toString()))
            .andExpect(jsonPath("$.initialTerm").value(DEFAULT_INITIAL_TERM.toString()))
            .andExpect(jsonPath("$.finalTerm").value(DEFAULT_FINAL_TERM.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCustomer() throws Exception {
        // Get the customer
        restCustomerMockMvc.perform(get("/api/customers/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCustomer() throws Exception {
        // Initialize the database
        customerService.save(customer);

        int databaseSizeBeforeUpdate = customerRepository.findAll().size();

        // Update the customer
        Customer updatedCustomer = customerRepository.findOne(customer.getId());
        updatedCustomer
                .cpf(UPDATED_CPF)
                .cnpj(UPDATED_CNPJ)
                .name(UPDATED_NAME)
                .securecompany(UPDATED_SECURECOMPANY)
                .licenseplate(UPDATED_LICENSEPLATE)
                .initialTerm(UPDATED_INITIAL_TERM)
                .finalTerm(UPDATED_FINAL_TERM);

        restCustomerMockMvc.perform(put("/api/customers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCustomer)))
                .andExpect(status().isOk());

        // Validate the Customer in the database
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customers.get(customers.size() - 1);
        assertThat(testCustomer.getCpf()).isEqualTo(UPDATED_CPF);
        assertThat(testCustomer.getCnpj()).isEqualTo(UPDATED_CNPJ);
        assertThat(testCustomer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCustomer.getSecurecompany()).isEqualTo(UPDATED_SECURECOMPANY);
        assertThat(testCustomer.getLicenseplate()).isEqualTo(UPDATED_LICENSEPLATE);
        assertThat(testCustomer.getInitialTerm()).isEqualTo(UPDATED_INITIAL_TERM);
        assertThat(testCustomer.getFinalTerm()).isEqualTo(UPDATED_FINAL_TERM);
    }

    @Test
    @Transactional
    public void deleteCustomer() throws Exception {
        // Initialize the database
        customerService.save(customer);

        int databaseSizeBeforeDelete = customerRepository.findAll().size();

        // Get the customer
        restCustomerMockMvc.perform(delete("/api/customers/{id}", customer.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(databaseSizeBeforeDelete - 1);
    }
}
