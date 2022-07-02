package com.scn.jira.automation.impl.condition;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.api.domain.service.PermissionProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserIsAdminCondition extends AbstractWebCondition {
    private final PermissionProvider permissionProvider;

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return permissionProvider.isCurrentUserAdmin();
    }
}
