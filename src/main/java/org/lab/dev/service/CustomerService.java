package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.Customer;
import org.lab.dev.repository.CustomerRepository;
import org.lab.dev.web.dto.CustomerDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    public static CustomerDto mapToDo(Customer customer){
        return new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getTelephone()
        );
    }

    public CustomerDto create(CustomerDto customerDto) {
        log.debug("Request create a new customer: {}", customerDto);
        return mapToDo(this.customerRepository.save(
                new Customer(
                        customerDto.getFirstName(),
                        customerDto.getLastName(),
                        customerDto.getEmail(),
                        customerDto.getTelephone(),
                        Collections.emptySet(),
                        Boolean.TRUE)
        ));
    }

    public List<CustomerDto> findAll() {
        log.debug("Request to get all Customers");
        return this.customerRepository.findAll()
                .stream()
                .map(CustomerService::mapToDo)
                .collect(Collectors.toList());
    }

    public CustomerDto findById(Long id) {
        log.debug("Request find by id: {}", id);
        return this.customerRepository.findById(id)
                .map(CustomerService::mapToDo).orElse(null);
    }

    public List<CustomerDto> findAllActive() {
        log.debug("Request find all active customers");
        return this.customerRepository.findAllByEnabled(true)
                .stream()
                .map(CustomerService::mapToDo)
                .collect(Collectors.toList());
    }

    public List<CustomerDto> findAllInActive() {
        log.debug("Request find all inactive customer");
        return this.customerRepository.findAllByEnabled(false)
                .stream()
                .map(CustomerService::mapToDo)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        log.debug("Request delete customer id: {}", id);

        var customer = this.customerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Cannot find this customer"));

        customer.setEnable(false);
        this.customerRepository.save(customer);
    }
}
