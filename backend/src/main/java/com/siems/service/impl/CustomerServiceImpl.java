package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.customer.CustomerRequest;
import com.siems.dto.customer.CustomerResponse;
import com.siems.entity.Customer;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.CustomerMapper;
import com.siems.repository.CustomerRepository;
import com.siems.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicateResourceException("Customer already exists with email: " + request.getContactEmail());
        }
        Customer customer = customerMapper.toEntity(request);
        if (customer.getCreditTerms() == null) {
            customer.setCreditTerms("NET_30");
        }
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findCustomerOrThrow(id));
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (!customer.getContactEmail().equalsIgnoreCase(request.getContactEmail())
                && customerRepository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicateResourceException("Another customer already uses email: " + request.getContactEmail());
        }

        customerMapper.updateEntityFromRequest(request, customer);
        Customer updated = customerRepository.save(customer);
        return customerMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(String keyword, Pageable pageable) {
        Page<Customer> page = customerRepository.search(keyword, pageable);
        return PageResponse.from(page.map(customerMapper::toResponse));
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }
}
