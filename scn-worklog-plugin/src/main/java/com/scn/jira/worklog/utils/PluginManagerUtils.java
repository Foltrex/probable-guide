package com.scn.jira.worklog.utils;

import com.atlassian.plugin.PluginController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PluginManagerUtils {
    private static final String ISSUE_OPERATIONS_LOG_WORK_MODULE_KEY = "com.atlassian.jira.plugin.system.issueoperations:log-work";
    private static final String ISSUE_TAB_PANELS_WORKLOG_MODULE_KEY = "com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel";
    private static final String VIEW_ISSUE_PANELS_TIMETRACKING_MODULE_KEY = "com.atlassian.jira.jira-view-issue-plugin:timetrackingmodule";

    private final List<String> moduleKeys = Arrays.asList(
        ISSUE_OPERATIONS_LOG_WORK_MODULE_KEY,
        ISSUE_TAB_PANELS_WORKLOG_MODULE_KEY,
        VIEW_ISSUE_PANELS_TIMETRACKING_MODULE_KEY
    );

    private final PluginController pluginController;

    public void disableSystemPlugins() {
        moduleKeys.forEach(pluginController::disablePluginModule);
    }

    public void enableSystemPlugins() {
        moduleKeys.forEach(pluginController::enablePluginModule);
    }
}
