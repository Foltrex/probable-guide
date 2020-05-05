package com.scn.jira.worklog.conditions;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;

import javax.inject.Inject;

public class ScnGlobalPermissionCondition extends AbstractWebCondition {
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
