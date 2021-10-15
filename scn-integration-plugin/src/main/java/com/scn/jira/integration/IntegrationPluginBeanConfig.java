package com.scn.jira.integration;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
//@Import({
//    ModuleFactoryBean.class,
//    PluginAccessorBean.class
//})
public class IntegrationPluginBeanConfig {
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public AvatarManager avatarManager() {
        return importOsgiService(AvatarManager.class);
    }

//    @Bean
//    public FieldLayoutManager fieldLayoutManager() {
//        return importOsgiService(FieldLayoutManager.class);
//    }
//
//    @Bean
//    public IssueSecuritySchemeManager issueSecuritySchemeManager() {
//        return importOsgiService(IssueSecuritySchemeManager.class);
//    }
}
