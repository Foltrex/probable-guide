package com.scn.jira.integration;

import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.scn.jira.cloneproject.CloneProjectPluginComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
    ModuleFactoryBean.class,
    PluginAccessorBean.class
})
public class IntegrationPluginBeanConfig {
    @Bean
    public CloneProjectPluginComponent cloneProjectPluginComponent() {
        return importOsgiService(CloneProjectPluginComponent.class);
    }
}
