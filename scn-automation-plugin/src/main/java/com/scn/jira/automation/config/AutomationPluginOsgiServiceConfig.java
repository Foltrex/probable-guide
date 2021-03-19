package com.scn.jira.automation.config;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.listener.PluginLauncher;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;

@Configuration
public class AutomationPluginOsgiServiceConfig {
    @Bean
    public FactoryBean<ServiceRegistration> registerPluginLauncher(final PluginLauncher pluginLauncher) {
        return exportOsgiService(pluginLauncher, null, LifecycleAware.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerWorklogContextService(final WorklogContextService worklogContextService) {
        return exportOsgiService(worklogContextService, null, WorklogContextService.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerAutoTTService(final AutoTTService autoTTService) {
        return exportOsgiService(autoTTService, null, AutoTTService.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerScnBIService(final ScnBIService scnBIService) {
        return exportOsgiService(scnBIService, null, ScnBIService.class);
    }
}
