package com.github.rmannibucau.quote.manager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
public class Quote {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(unique = true)
    private String name;

    private double value;

    @ManyToMany
    private Set<Customer> customers;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(final Set<Customer> customers) {
        this.customers = customers;
    }
}
