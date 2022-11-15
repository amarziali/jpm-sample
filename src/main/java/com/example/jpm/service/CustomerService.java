package com.example.jpm.service;

import com.example.jpm.model.Customer;
import com.example.jpm.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This service uses spring data approach (repository based interaction) to query OpenSearch
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository repository) {
        customerRepository = repository;
    }

    public Mono<List<Customer>> findAll() {
        return customerRepository.findAll();
    }

    public Mono<List<Customer>> findByFirstName(String firstName) {
        return customerRepository.findByFirstName(firstName);
    }

    public Mono<List<Customer>> findByLastName(String lastName) {
        return customerRepository.findByLastName(lastName);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Iterable<Customer> saveAll(List<Customer> customers) {
        return customerRepository.saveAll(customers);
    }

    public Mono<List<Customer>> fetchProductNamesContaining(final String name) {
        return customerRepository.findByFirstNameContaining(name);
    }

}
