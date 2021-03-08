package com.scn.jira.cloneproject;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
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
public class CloneProjectPluginBeanConfig {
    @Bean
    public AvatarManager avatarManager() {
        return importOsgiService(AvatarManager.class);
    }

    @Bean
    public FieldLayoutManager fieldLayoutManager() {
        return importOsgiService(FieldLayoutManager.class);
    }

    @Bean
    public IssueSecuritySchemeManager issueSecuritySchemeManager() {
        return importOsgiService(IssueSecuritySchemeManager.class);
    }

    @Bean
    public IssueTypeSchemeManager issueTypeSchemeManager() {
        return importOsgiService(IssueTypeSchemeManager.class);
    }

    @Bean
    public IssueTypeScreenSchemeManager issueTypeScreenSchemeManager() {
        return importOsgiService(IssueTypeScreenSchemeManager.class);
    }

    @Bean
    public NotificationSchemeManager notificationSchemeManager() {
        return importOsgiService(NotificationSchemeManager.class);
    }

    @Bean
    public PermissionSchemeManager permissionSchemeManager() {
        return importOsgiService(PermissionSchemeManager.class);
    }

    @Bean
    public ProjectManager projectManager() {
        return importOsgiService(ProjectManager.class);
    }

    @Bean
    public ProjectService projectService() {
        return importOsgiService(ProjectService.class);
    }

    @Bean
    public UserManager userManager() {
        return importOsgiService(UserManager.class);
    }

    @Bean
    public WorkflowSchemeManager workflowSchemeManager() {
        return importOsgiService(WorkflowSchemeManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerOsgiService(final CloneProjectPluginComponent cloneProjectPluginComponent) {
        return exportOsgiService(cloneProjectPluginComponent, null, CloneProjectPluginComponent.class);
    }
}
