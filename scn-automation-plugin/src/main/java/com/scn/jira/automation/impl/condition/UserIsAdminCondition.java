package com.scn.jira.automation.impl.condition;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.scn.jira.automation.api.domain.service.JiraContextService;

@Scanned
public class UserIsAdminCondition extends AbstractWebCondition {
    private final JiraContextService jiraContextService;

    public UserIsAdminCondition(JiraContextService jiraContextService) {
        this.jiraContextService = jiraContextService;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return jiraContextService.isCurrentUserAdmin();
    }
}
