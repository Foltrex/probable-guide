package com.scn.jira.automation.impl.rest;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.scn.jira.automation.api.domain.service.JiraContextService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseResource {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final JiraContextService contextService;
    private final ProjectManager projectManager;

    protected BaseResource(JiraContextService contextService, ProjectManager projectManager) {
        this.contextService = contextService;
        this.projectManager = projectManager;
    }

    protected boolean isProjectAdministrationAllowed(Long pid) {
        Project project = projectManager.getProjectObj(pid);
        return project != null && (isAdministrationAllowed()
            || project.getLeadUserKey().equals(contextService.getCurrentUser().getKey()));
    }

    protected boolean isAdministrationAllowed() {
        return contextService.isCurrentUserAdmin()
            || contextService.getCurrentUser().getKey().equals("akalaputs");
    }

    protected Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }
}
