package com.scn.jira.integration;

import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;

@Configuration
public class IntegrationPluginOsgiServiceConfig {
    @Bean
    public FactoryBean<ServiceRegistration> registerIntegrationPluginComponent(final IntegrationPluginComponent integrationPluginComponent) {
        return exportOsgiService(integrationPluginComponent, null, IntegrationPluginComponent.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerTestComponent(final TestComponent testComponent) {
        return exportOsgiService(testComponent, null, TestComponent.class);
    }
}
