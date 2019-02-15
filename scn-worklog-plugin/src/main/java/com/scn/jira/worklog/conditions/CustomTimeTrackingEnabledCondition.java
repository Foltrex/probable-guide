package com.scn.jira.worklog.conditions;

import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.TimeTrackingEnabledCondition;

public class CustomTimeTrackingEnabledCondition extends TimeTrackingEnabledCondition {

    public CustomTimeTrackingEnabledCondition() {
        super(ComponentAccessor.getComponent(WorklogService.class));
    }
}
