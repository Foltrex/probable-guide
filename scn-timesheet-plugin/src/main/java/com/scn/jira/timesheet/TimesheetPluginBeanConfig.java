package com.scn.jira.timesheet;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
    ModuleFactoryBean.class,
    PluginAccessorBean.class
})
public class TimesheetPluginBeanConfig {
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public ConstantsManager constantsManager() {
        return importOsgiService(ConstantsManager.class);
    }

    @Bean
    public DateTimeFormatterFactory dateTimeFormatterFactory() {
        return importOsgiService(DateTimeFormatterFactory.class);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return importOsgiService(EventPublisher.class);
    }

    @Bean
    public FieldVisibilityManager fieldVisibilityManager() {
        return importOsgiService(FieldVisibilityManager.class);
    }

    @Bean
    public FieldManager fieldManager() {
        return importOsgiService(FieldManager.class);
    }

    @Bean
    public GroupManager groupManager() {
        return importOsgiService(GroupManager.class);
    }

    @Bean
    public GlobalPermissionManager globalPermissionManager() {
        return importOsgiService(GlobalPermissionManager.class);
    }

    @Bean
    public GroupPickerSearchService groupPickerSearchService() {
        return importOsgiService(GroupPickerSearchService.class);
    }

    @Bean
    public IssueManager issueManager() {
        return importOsgiService(IssueManager.class);
    }

    @Bean
    public IGlobalSettingsManager iGlobalSettingsManager() {
        return importOsgiService(IGlobalSettingsManager.class);
    }

    @Bean
    public JiraAuthenticationContext jiraAuthenticationContext() {
        return importOsgiService(JiraAuthenticationContext.class);
    }

    @Bean
    public PermissionManager permissionManager() {
        return importOsgiService(PermissionManager.class);
    }

    @Bean
    public ProjectManager projectManager() {
        return importOsgiService(ProjectManager.class);
    }

    @Bean
    public ProjectRoleManager projectRoleManager() {
        return importOsgiService(ProjectRoleManager.class);
    }

    @Bean
    public SearchProvider searchProvider() {
        return importOsgiService(SearchProvider.class);
    }

    @Bean
    public SearchRequestManager searchRequestManager() {
        return importOsgiService(SearchRequestManager.class);
    }

    @Bean
    public SearchRequestService searchRequestService() {
        return importOsgiService(SearchRequestService.class);
    }

    @Bean
    public UserSearchService userSearchService() {
        return importOsgiService(UserSearchService.class);
    }

    @Bean
    public UserManager userManager() {
        return importOsgiService(UserManager.class);
    }

    @Bean
    public VisibilityValidator visibilityValidator() {
        return importOsgiService(VisibilityValidator.class);
    }
}
