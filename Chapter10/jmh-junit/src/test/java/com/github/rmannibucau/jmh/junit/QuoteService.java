package com.github.rmannibucau.jmh.junit;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

// you can reuse quote-manager impl
@ApplicationScoped
public class QuoteService {
    public Optional<String> findByName(final String name) {
        return Optional.empty();
    }
}
