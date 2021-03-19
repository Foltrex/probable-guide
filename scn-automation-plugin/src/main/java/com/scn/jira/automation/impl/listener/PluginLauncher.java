package com.scn.jira.automation.impl.listener;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.automation.api.AutomationPluginComponent;
import com.scn.jira.automation.impl.service.AutoTTExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_CRON_SCHEDULE;
import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_NAME;

@Component
@RequiredArgsConstructor
@Log4j
@PublicApi
public class PluginLauncher implements LifecycleAware {
    private final ServiceManager serviceManager;
    private final AutomationPluginComponent automationPluginComponent;

    @Override
    public void onStart() {
        log.warn(automationPluginComponent.getName() + " has been started.");
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
                    log.error(e.getMessage());
                }
            });
    }

    private void createServices() {
        try {
            serviceManager.addService(DEFAULT_NAME, AutoTTExecutionService.class, DEFAULT_CRON_SCHEDULE);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }
}
