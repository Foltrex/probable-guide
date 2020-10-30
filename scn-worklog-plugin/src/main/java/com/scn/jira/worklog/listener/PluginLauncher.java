package com.scn.jira.worklog.listener;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@ExportAsService
public class PluginLauncher implements LifecycleAware {
    private static final Date BLOCKING_DATE = new Date(1577826000000L);
    private final ProjectManager projectManager;
    private final IScnProjectSettingsManager projectSettingManager;

    @Autowired
    public PluginLauncher(ProjectManager projectManager, IScnProjectSettingsManager projectSettingManager) {
        this.projectManager = projectManager;
        this.projectSettingManager = projectSettingManager;
    }

    @Override
    public void onStart() {
        List<Project> projects = projectManager.getProjectObjects();
        projects.forEach(project -> {
            projectSettingManager.setWLTypeRequired(project.getId(), true);
            Date currentBlockingDate = projectSettingManager.getWLBlockingDate(project.getId());
            if (currentBlockingDate == null || currentBlockingDate.before(BLOCKING_DATE)) {
                projectSettingManager.setWLBlockingDate(project.getId(), BLOCKING_DATE);
            }
        });
    }

    @Override
    public void onStop() {
    }
}
