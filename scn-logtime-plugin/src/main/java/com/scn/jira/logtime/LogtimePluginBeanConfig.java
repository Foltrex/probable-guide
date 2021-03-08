package com.scn.jira.logtime;

import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
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
public class LogtimePluginBeanConfig {
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public ExtendedWorklogManager extendedWorklogManager() {
        return importOsgiService(ExtendedWorklogManager.class);
    }

    @Bean
    public ExtendedConstantsManager extendedConstantsManager() {
        return importOsgiService(ExtendedConstantsManager.class);
    }

    @Bean
    public IGlobalSettingsManager iGlobalSettingsManager() {
        return importOsgiService(IGlobalSettingsManager.class);
    }

    @Bean
    public IssueManager issueManager() {
        return importOsgiService(IssueManager.class);
    }

    @Bean
    public IScnWorklogManager iScnWorklogManager() {
        return importOsgiService(IScnWorklogManager.class);
    }

    @Bean
    public IScnWorklogService iScnWorklogService() {
        return importOsgiService(IScnWorklogService.class);
    }

    @Bean
    public JiraAuthenticationContext jiraAuthenticationContext() {
        return importOsgiService(JiraAuthenticationContext.class);
    }

    @Bean
    public WorklogManager overridedWorklogManager() {
        return importOsgiService(OverridedWorklogManager.class);
    }

    @Bean
    public OfBizScnWorklogStore ofBizScnWorklogStore() {
        return importOsgiService(OfBizScnWorklogStore.class);
    }

    @Bean
    public PermissionManager permissionManager() {
        return importOsgiService(PermissionManager.class);
    }

    @Bean
    public ProjectRoleManager projectRoleManager() {
        return importOsgiService(ProjectRoleManager.class);
    }

    @Bean
    public ProjectManager projectManager() {
        return importOsgiService(ProjectManager.class);
    }

    @Bean
    public IScnProjectSettingsManager scnProjectSettingsManager() {
        return importOsgiService(IScnProjectSettingsManager.class);
    }

    @Bean
    public IScnUserBlockingManager iScnUserBlockingManager() {
        return importOsgiService(IScnUserBlockingManager.class);
    }

    @Bean
    public UserManager userManager() {
        return importOsgiService(UserManager.class);
    }

    @Bean
    public WorklogService worklogService() {
        return importOsgiService(WorklogService.class);
    }
}
