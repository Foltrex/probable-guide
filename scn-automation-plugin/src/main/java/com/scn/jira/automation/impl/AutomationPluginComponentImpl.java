package com.scn.jira.automation.impl;

import com.scn.jira.automation.api.AutomationPluginComponent;
import org.springframework.stereotype.Component;

@Component
public class AutomationPluginComponentImpl implements AutomationPluginComponent {
    @Override
    public String getName() {
        return "Automation Plugin";
    }
}
