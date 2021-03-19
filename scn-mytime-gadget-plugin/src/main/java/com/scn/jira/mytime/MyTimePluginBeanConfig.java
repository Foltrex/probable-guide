package com.scn.jira.mytime;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
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
public class MyTimePluginBeanConfig {
    @Bean
    public IssueManager issueManager() {
        return importOsgiService(IssueManager.class);
    }

    @Bean
    public GlobalPermissionManager globalPermissionManager() {
        return importOsgiService(GlobalPermissionManager.class);
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
    public PermissionManager permissionManager() {
        return importOsgiService(PermissionManager.class);
    }

    @Bean
    public ProjectRoleManager projectRoleManager() {
        return importOsgiService(ProjectRoleManager.class);
    }
}
