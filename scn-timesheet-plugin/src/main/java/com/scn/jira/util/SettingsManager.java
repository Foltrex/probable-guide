package com.scn.jira.util;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsManager extends GlobalSettingsManager {

    @Autowired
    public SettingsManager(@ComponentImport GroupManager groupManager, @ComponentImport PermissionManager permissionManager) {
        super(groupManager, permissionManager);
    }
}
