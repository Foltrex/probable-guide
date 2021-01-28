package com.scn.jira.logtime;

import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import com.scn.jira.worklog.wl.OverridedWorklogManager;
import org.springframework.stereotype.Component;

@Component
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private ApplicationProperties applicationProperties;
    @ComponentImport
    private ExtendedWorklogManager extendedWorklogManager;
    @ComponentImport
    private ExtendedConstantsManager extendedConstantsManager;
    @ComponentImport
    private IGlobalSettingsManager iGlobalSettingsManager;
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private IScnWorklogManager iScnWorklogManager;
    @ComponentImport
    private IScnWorklogService iScnWorklogService;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    private OverridedWorklogManager overridedWorklogManager;
    @ComponentImport
    private OfBizScnWorklogStore ofBizScnWorklogStore;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private ProjectRoleManager projectRoleManager;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private ScnProjectSettingsManager scnProjectSettingsManager;
    @ComponentImport
    private IScnUserBlockingManager iScnUserBlockingManager;
    @ComponentImport
    private WorklogManager worklogManager;
    @ComponentImport
    private WorklogService worklogService;


    public String getName() {
        return "Logtime plugin";
    }
}
