package com.github.rmannibucau.quote.manager.service;

import javax.ejb.Lock;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import static javax.ejb.LockType.WRITE;

@Singleton
@Lock(WRITE)
public class DataRefresher {
    @Inject
    private ProvisioningService provisioningService;

    @Schedule(hour = "*", persistent = false, info = "refresh-quotes")
    public void refresh() {
        provisioningService.refresh();
    }
}
