package com.scn.jira.mytime;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.wl.OverridedWorklogManager;
import org.springframework.stereotype.Component;

@Component
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private GlobalPermissionManager globalPermissionManager;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
//    @ComponentImport
//    private OverridedWorklogManager overridedWorklogManager;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private ProjectRoleManager projectRoleManager;
    @ComponentImport
    private WorklogManager worklogManager;

    public String getName() {
        return "My time plugin";
    }
}
