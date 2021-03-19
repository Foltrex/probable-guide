package com.scn.jira.timesheet;

import org.springframework.stereotype.Component;

@Component
public class TimesheetPluginComponentImpl implements TimesheetPluginComponent {
    @Override
    public String getName() {
        return "Timesheet plugin";
    }
}
