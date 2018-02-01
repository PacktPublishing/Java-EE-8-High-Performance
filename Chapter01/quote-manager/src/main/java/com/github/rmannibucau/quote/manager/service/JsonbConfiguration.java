package com.github.rmannibucau.quote.manager.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
public class JsonbConfiguration {
    @Produces
    @ApplicationScoped
    public Jsonb jsonb() {
        return JsonbBuilder.create();
    }
}
