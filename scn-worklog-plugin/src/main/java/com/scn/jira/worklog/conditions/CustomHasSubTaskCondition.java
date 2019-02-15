package com.scn.jira.worklog.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.viewissue.HasSubTaskCondition;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.inject.Inject;

public class CustomHasSubTaskCondition extends HasSubTaskCondition{

    @Inject
    public CustomHasSubTaskCondition(JiraAuthenticationContext authenticationContext) {
        super(ComponentAccessor.getSubTaskManager(), authenticationContext);
    }
}
