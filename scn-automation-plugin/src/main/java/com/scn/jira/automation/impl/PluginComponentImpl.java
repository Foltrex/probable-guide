package com.scn.jira.automation.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.scn.jira.automation.api.PluginComponent;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.springframework.stereotype.Component;

@Component
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private ActiveObjects ao;
    @ComponentImport
    private ExtendedConstantsManager extendedConstantsManager;
    @ComponentImport
    private GlobalPermissionManager globalPermissionManager;
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private IScnWorklogService scnDefaultWorklogService;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    private OfBizDelegator ofBizDelegator;
    @ComponentImport
    private PageBuilderService pageBuilderService;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private ProjectRoleManager projectRoleManager;
    @ComponentImport
    private ScnProjectSettingsManager projectSettingsManager;
    @ComponentImport
    private ServiceManager serviceManager;
    @ComponentImport
    private UserManager userManager;

    @Override
    public String getName() {
        return "ScienceSoft Automation Plugin";
    }
}
