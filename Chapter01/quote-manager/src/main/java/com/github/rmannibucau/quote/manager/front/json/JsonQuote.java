package com.github.rmannibucau.quote.manager.front.json;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder({"id", "name", "customerCount"})
public class JsonQuote {
    private long id;
    private String name;
    private double value;

    @JsonbProperty("customer_count")
    private long customerCount;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(final long customerCount) {
        this.customerCount = customerCount;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }
}
