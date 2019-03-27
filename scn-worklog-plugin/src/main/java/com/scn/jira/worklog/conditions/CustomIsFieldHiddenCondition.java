package com.scn.jira.worklog.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.IsFieldHiddenCondition;
import com.atlassian.jira.web.FieldVisibilityManager;

public class CustomIsFieldHiddenCondition extends IsFieldHiddenCondition {

    public CustomIsFieldHiddenCondition() {
        super(ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }
}
