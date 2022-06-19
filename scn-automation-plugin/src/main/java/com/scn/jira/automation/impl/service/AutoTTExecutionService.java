package com.scn.jira.automation.impl.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.AbstractService;
import com.opensymphony.module.propertyset.PropertySet;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import lombok.extern.log4j.Log4j;

@Log4j
public class AutoTTExecutionService extends AbstractService {
    public static final String DEFAULT_NAME = "Auto time tracking";
    public static final String DEFAULT_CRON_SCHEDULE = "0 0 * * * ?";

    private AutoTTService autoTTService;

    @Override
    public void init(PropertySet props, long configurationIdentifier) throws ObjectConfigurationException {
        autoTTService = ComponentAccessor.getOSGiComponentInstanceOfType(AutoTTService.class);
    }

    @Override
    public void run() {
        autoTTService.startJob();
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return this.getObjectConfiguration("AutoTTExecutionService", "services/AutoTTExecutionService.xml", null);
    }
}
