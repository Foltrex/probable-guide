package com.scn.jira.worklog.listener;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.worklog.api.WorklogPluginComponent;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
@PublicApi
public class PluginLauncher implements LifecycleAware {
    //    private static final Date BLOCKING_DATE = new Date(1577826000000L);
    private final ProjectManager projectManager;
    private final IScnProjectSettingsManager projectSettingManager;
    private final WorklogPluginComponent worklogPluginComponent;

    @Override
    public void onStart() {
        log.warn(worklogPluginComponent.getName() + " has been started.");
        //updateProjectsSettings();
    }

    @Override
    public void onStop() {
    }

    private void updateProjectsSettings() {
        List<Project> projects = projectManager.getProjectObjects();
        projects.forEach(project -> projectSettingManager.setWLTypeRequired(project.getId(), true));
    }
}
