package com.scn.jira.worklog.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.SubTasksEnabledCondition;

public class CustomSubTasksEnabledCondition extends SubTasksEnabledCondition {

    public CustomSubTasksEnabledCondition() {
        super(ComponentAccessor.getSubTaskManager());
    }
}
