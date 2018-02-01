package com.github.rmannibucau.quote.manager.front.json;

import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder({"id", "name"})
public class JsonCustomer {
    private long id;
    private String name;

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
}
