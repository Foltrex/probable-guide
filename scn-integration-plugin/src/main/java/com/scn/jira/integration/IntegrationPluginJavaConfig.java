package com.scn.jira.integration;

import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.scn.jira.cloneproject.CloneProjectPluginComponent;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;
import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
    ModuleFactoryBean.class,
    PluginAccessorBean.class
})
public class IntegrationPluginJavaConfig {
    @Bean
    public CloneProjectPluginComponent cloneProjectPluginComponent() {
        return importOsgiService(CloneProjectPluginComponent.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerOsgiService(final IntegrationPluginComponent integrationPluginComponent) {
        return exportOsgiService(integrationPluginComponent, null, IntegrationPluginComponent.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerOsgiService(final TestComponent testComponent) {
        return exportOsgiService(testComponent, null, TestComponent.class);
    }
}
