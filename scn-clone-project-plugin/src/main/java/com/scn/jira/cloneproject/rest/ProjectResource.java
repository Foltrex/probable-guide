package com.scn.jira.cloneproject.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Named
@Path("/project")
@RequiredArgsConstructor
public class ProjectResource {
    private final ProjectService projectService;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @PublicApi
    public CloneProjectResourceModel getProject(@QueryParam("pkey") String pkey) throws Exception {
        Project project = projectService.getProjectByKey(pkey).get();
        if (project == null) {
            throw new Exception("Project not exists or insufficient permissions");
        }
        CloneProjectResourceModel data = new CloneProjectResourceModel("OK",
            project.getKey(),
            project.getName(),
            project.getLeadUserName(),
            project.getDescription(),
            project.getUrl(),
            project.getAssigneeType(),
            project.getProjectCategoryObject() == null ? null : project.getProjectCategoryObject().getName(),
            workflowSchemeManager.getSchemeFor(project) == null ? null : workflowSchemeManager.getSchemeFor(project).getName(),
            issueTypeSchemeManager.getConfigScheme(project) == null ? null : issueTypeSchemeManager.getConfigScheme(project).getName(),
            issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project) == null ? null : issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project).getName(),
            fieldLayoutManager.getFieldConfigurationScheme(project) == null ? null : fieldLayoutManager.getFieldConfigurationScheme(project).getName(),
            permissionSchemeManager.getSchemeFor(project) == null ? null : permissionSchemeManager.getSchemeFor(project).getName(),
            notificationSchemeManager.getSchemeFor(project) == null ? null : notificationSchemeManager.getSchemeFor(project).getName(),
            issueSecuritySchemeManager.getSchemeFor(project) == null ? null : issueSecuritySchemeManager.getSchemeFor(project).getName());
        return data;
    }

    @DELETE
    @Produces("text/plain")
    @PublicApi
    public String deleteProject(@QueryParam("pkey") String pkey) throws Exception {
        Project project = projectService.getProjectByKey(pkey).get();
        if (project == null) {
            throw new Exception("Project not exists or insufficient permissions");
        }
        ProjectService.DeleteProjectValidationResult result =
            projectService.validateDeleteProject(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), pkey);
        if (!result.isValid()) {
            throw new Exception("Project not exists or insufficient permissions");
        }
        ProjectService.DeleteProjectResult deleteResult = projectService.deleteProject(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), result);
        if (!deleteResult.isValid()) {
            throw new Exception("Project not exists or insufficient permissions");
        }
        return "OK";
    }
}

