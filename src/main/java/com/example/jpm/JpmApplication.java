package com.example.jpm;

import com.example.jpm.model.Customer;
import com.example.jpm.repository.CustomerRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

@SpringBootApplication
public class JpmApplication {

    @Autowired
    private CustomerRepository customerRepository;


    public static void main(String[] args) {
        SpringApplication.run(JpmApplication.class, args);
    }

    @PostConstruct
    public void buildIndex() {
        customerRepository.saveAll(prepareDataset());
    }

    private List<Customer> prepareDataset() {
        //Generate a fake dataset using Faker library
        Faker faker = new Faker();
        SplittableRandom random = new SplittableRandom();
        String firstName;
        String lastName;
        int age;
        //generate 200 customer names using Faker
        List<Customer> customerList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            firstName = faker.name().firstName(); // Emory
            lastName = faker.name().lastName(); // Barton
            age = random.nextInt(10, 30);
            customerList.add(Customer.builder().age(age).firstName(firstName).lastName(lastName).build());
        }
        return customerList;
    }
}
