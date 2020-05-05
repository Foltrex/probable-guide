package com.scn.jira.plugin;

import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import org.springframework.stereotype.Component;

@Component
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private ApplicationProperties applicationProperties;
    @ComponentImport
    private FieldVisibilityManager fieldVisibilityManager;
    @ComponentImport
    private GroupManager groupManager;
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private IGlobalSettingsManager iGlobalSettingsManager;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private ProjectRoleManager projectRoleManager;
    @ComponentImport
    private SearchProvider searchProvider;
    @ComponentImport
    private SearchRequestManager searchRequestManager;
    @ComponentImport
    private UserManager userManager;
    @ComponentImport
    private VisibilityValidator visibilityValidator;

    @Override
    public String getName() {
        return "Timesheet plugin";
    }
}
