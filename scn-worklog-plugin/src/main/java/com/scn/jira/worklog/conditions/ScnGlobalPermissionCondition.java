package com.scn.jira.worklog.conditions;

import javax.inject.Inject;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;

public class ScnGlobalPermissionCondition extends AbstractWebCondition {
    @ComponentImport
    private final IGlobalSettingsManager gpManager;

    @Inject
    public ScnGlobalPermissionCondition(IGlobalSettingsManager gpManager) {
        this.gpManager = gpManager;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser user, JiraHelper jHelper) {
        if (user == null) return false;

        return gpManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
    }
}
