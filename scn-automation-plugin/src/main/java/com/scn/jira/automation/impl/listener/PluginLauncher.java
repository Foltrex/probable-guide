package com.scn.jira.automation.impl.listener;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.automation.impl.service.AutoTTExecutionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_CRON_SCHEDULE;
import static com.scn.jira.automation.impl.service.AutoTTExecutionService.DEFAULT_NAME;

@Component
@ExportAsService
public class PluginLauncher implements LifecycleAware {
    private static final Logger LOGGER = Logger.getLogger(PluginLauncher.class);
    private final ServiceManager serviceManager;
    private final ActiveObjects ao;

    @Autowired
    public PluginLauncher(ServiceManager serviceManager, ActiveObjects ao) {
        this.serviceManager = serviceManager;
        this.ao = ao;
    }

    @Override
    public void onStart() {
        ao.migrate(AutoTT.class);
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
                    LOGGER.error(e.getMessage());
                }
            });
    }

    private void createServices() {
        try {
            serviceManager.addService(DEFAULT_NAME, AutoTTExecutionService.class, DEFAULT_CRON_SCHEDULE);
        } catch (ServiceException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
