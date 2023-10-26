package com.scn.jira.worklog.listener;

import com.atlassian.annotations.PublicApi;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.worklog.api.WorklogPluginComponent;
import com.scn.jira.worklog.utils.PluginManagerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j
@PublicApi
public class WorklogPluginLauncher implements LifecycleAware {

    private final WorklogPluginComponent worklogPluginComponent;
    private final PluginManagerUtils pluginManagerUtils;

    @Override
    public void onStart() {
        pluginManagerUtils.disableSystemPlugins();
        log.warn(worklogPluginComponent.getName() + " has been started.");
    }

    @Override
    public void onStop() {
//        pluginManagerUtils.enableSystemPlugins();
    }
}
