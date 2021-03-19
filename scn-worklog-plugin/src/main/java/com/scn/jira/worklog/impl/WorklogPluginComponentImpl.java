package com.scn.jira.worklog.impl;

import com.scn.jira.worklog.api.WorklogPluginComponent;
import org.springframework.stereotype.Component;

@Component
public class WorklogPluginComponentImpl implements WorklogPluginComponent {
    @Override
    public String getName() {
        return "Worklog Plugin";
    }
}
