package com.scn.cloneproject;

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
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Named;

@Named
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private AvatarManager avatarManager;
    @ComponentImport
    private FieldLayoutManager fieldLayoutManager;
    @ComponentImport
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    @ComponentImport
    private IssueTypeSchemeManager issueTypeSchemeManager;
    @ComponentImport
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    @ComponentImport
    private NotificationSchemeManager notificationSchemeManager;
    @ComponentImport
    private PermissionSchemeManager permissionSchemeManager;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private ProjectService projectService;
    @ComponentImport
    private UserManager userManager;
    @ComponentImport
    private WorkflowSchemeManager workflowSchemeManager;

    public String getName() {
        return "Clone project plugin";
    }
}
