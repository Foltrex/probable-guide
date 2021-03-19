package com.scn.jira.logtime;

import org.springframework.stereotype.Component;

@Component
public class LogtimePluginComponentImpl implements LogtimePluginComponent {
    public String getName() {
        return "Logtime Plugin";
    }
}
