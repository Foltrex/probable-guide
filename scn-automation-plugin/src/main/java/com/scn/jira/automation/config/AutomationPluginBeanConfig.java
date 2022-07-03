package com.scn.jira.automation.config;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.scn.jira.common.exception.ObjectValidator;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import com.scn.jira.worklog.wl.OverridedWorklogManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
    ModuleFactoryBean.class,
    PluginAccessorBean.class
})
public class AutomationPluginBeanConfig {
    @Bean
    public ActiveObjects ao() {
        return importOsgiService(ActiveObjects.class);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return importOsgiService(EventPublisher.class);
    }

    @Bean
    public ExtendedConstantsManager extendedConstantsManager() {
        return importOsgiService(ExtendedConstantsManager.class);
    }

    @Bean
    public ExtendedWorklogManager extendedWorklogManager() {
        return importOsgiService(ExtendedWorklogManager.class);
    }

    @Bean
    public GlobalPermissionManager globalPermissionManager() {
        return importOsgiService(GlobalPermissionManager.class);
    }

    @Bean
    public IssueManager issueManager() {
        return importOsgiService(IssueManager.class);
    }

    @Bean
    public IScnWorklogService scnDefaultWorklogService() {
        return importOsgiService(IScnWorklogService.class);
    }

    @Bean
    public JiraAuthenticationContext jiraAuthenticationContext() {
        return importOsgiService(JiraAuthenticationContext.class);
    }

    @Bean
    public JiraDurationUtils jiraDurationUtils() {
        return ComponentAccessor.getComponent(JiraDurationUtils.class);
    }

    @Bean
    public ObjectValidator objectValidator() {
        return new ObjectValidator();
    }

    @Bean
    public OfBizDelegator ofBizDelegator() {
        return importOsgiService(OfBizDelegator.class);
    }

    @Bean
    public WorklogManager overridedWorklogManager() {
        return importOsgiService(OverridedWorklogManager.class);
    }

    @Bean
    public PageBuilderService pageBuilderService() {
        return importOsgiService(PageBuilderService.class);
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
    public ScnProjectSettingsManager projectSettingsManager() {
        return importOsgiService(ScnProjectSettingsManager.class);
    }

    @Bean
    public ServiceManager serviceManager() {
        return importOsgiService(ServiceManager.class);
    }

    @Bean
    public UserManager userManager() {
        return importOsgiService(UserManager.class);
    }
}
