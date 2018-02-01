package com.github.rmannibucau.quote.manager.service.startup;

import com.github.rmannibucau.quote.manager.service.ProvisioningService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;

@ApplicationScoped
public class InitialProvisioning {
    @Inject
    private ProvisioningService provisioningService;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) final ServletContext context) {
        provisioningService.refresh();
    }
}
