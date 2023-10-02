package com.scn.jira.worklog.utils;

import com.atlassian.plugin.PluginController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PluginManagerUtils {

    private final List<String> moduleKeys = Arrays.asList(
        "com.atlassian.jira.plugin.system.issueoperations:log-work",
        "com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel",
        "com.atlassian.jira.jira-view-issue-plugin:timetrackingmodule",
        "com.atlassian.jira.jira-view-issue-plugin:view-subtasks",
        "com.atlassian.jira.jira-view-issue-plugin:quick-add-subtask",
        "com.atlassian.jira.jira-view-issue-plugin:subtask-view-options",
        "com.atlassian.jira.jira-view-issue-plugin:subtask-view-options-factory",
        "com.atlassian.jira.jira-view-issue-plugin:subtask-view-progress"
    );

    private final PluginController pluginController;

    public void disableSystemPlugins() {
        moduleKeys.forEach(pluginController::disablePluginModule);
    }

    public void enableSystemPlugins() {
        moduleKeys.forEach(pluginController::enablePluginModule);
    }
}
