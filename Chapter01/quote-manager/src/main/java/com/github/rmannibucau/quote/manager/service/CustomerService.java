package com.github.rmannibucau.quote.manager.service;

import com.github.rmannibucau.quote.manager.model.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Function;

@Transactional
@ApplicationScoped
public class CustomerService {
    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Customer> findById(final long id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id));
    }

    public Customer create(final Customer newQuote) {
        entityManager.persist(newQuote);
        entityManager.flush();
        return newQuote;
    }

    public void delete(final long id) {
        entityManager.remove(findById(id).orElseThrow(() -> new IllegalArgumentException("Customer #" + id + " not found")));
        entityManager.flush();
    }

    public <T> T mutate(final long id, final Function<Optional<Customer>, T> callback) {
        return callback.apply(findById(id));
    }
}
