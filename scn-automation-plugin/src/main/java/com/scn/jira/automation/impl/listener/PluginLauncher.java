package com.scn.jira.automation.impl.listener;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.service.AutoTTExecutionService;
import com.scn.jira.common.exception.InternalRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_CRON_SCHEDULE;
import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_NAME;

@Component
@RequiredArgsConstructor
@Log4j
@PublicApi
public class PluginLauncher implements LifecycleAware {
    private final ServiceManager serviceManager;
    private final AutoTTService autoTTService;
    private final ActiveObjects activeObjects;
    private final WorklogContextService worklogContextService;

    @Override
    public void onStart() {
        CompletableFuture.runAsync(() -> {
            int count = 0;
            boolean isInitialized = false;
            while (!isInitialized && count < 10) {
                try {
                    activeObjects.moduleMetaData().awaitInitialization(1, TimeUnit.MINUTES);
                    isInitialized = activeObjects.moduleMetaData().isInitialized();
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    count++;
                }
            }
            autoTTService.removeAllByInvalidConstraint();
            worklogContextService.deleteIncorrectWorklogs();
        });
        removeServicesIfExist();
        createServices();
    }

    @Override
    public void onStop() {
        removeServicesIfExist();
    }

    private void removeServicesIfExist() {
        serviceManager.getServices().stream()
            .filter(container -> container.getServiceClass().equals(AutoTTExecutionService.class.getName()))
            .forEach(container -> {
                try {
                    serviceManager.removeService(container.getId());
                } catch (Exception e) {
                    throw new InternalRuntimeException(e);
                }
            });
    }

    private void createServices() {
        try {
            serviceManager.addService(DEFAULT_NAME, AutoTTExecutionService.class, DEFAULT_CRON_SCHEDULE);
        } catch (ServiceException e) {
            throw new InternalRuntimeException(e);
        }
    }
}
