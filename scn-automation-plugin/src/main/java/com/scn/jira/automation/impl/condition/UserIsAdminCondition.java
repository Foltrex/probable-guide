package com.scn.jira.automation.impl.condition;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserIsAdminCondition extends AbstractWebCondition {
    private final JiraContextService jiraContextService;

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return jiraContextService.isCurrentUserAdmin();
    }
}
