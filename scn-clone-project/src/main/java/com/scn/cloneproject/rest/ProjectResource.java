package com.scn.cloneproject.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

@Path("/project")
public class ProjectResource {
	private ProjectManager projectManager;
	private WorkflowSchemeManager workflowSchemeManager;
	private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private FieldLayoutManager fieldLayoutManager;
	private PermissionSchemeManager permissionSchemeManager;
	private NotificationSchemeManager notificationSchemeManager;
	private IssueSecuritySchemeManager issueSecuritySchemeManager;
	private IssueTypeSchemeManager issueTypeSchemeManager;

	public ProjectResource(ProjectManager projectManager, WorkflowSchemeManager workflowSchemeManager,
						   IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldLayoutManager fieldLayoutManager,
						   PermissionSchemeManager permissionSchemeManager, NotificationSchemeManager notificationSchemeManager,
						   IssueSecuritySchemeManager issueSecuritySchemeManager, IssueTypeSchemeManager issueTypeSchemeManager)
	{
		this.projectManager = projectManager;
		this.workflowSchemeManager = workflowSchemeManager;
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.fieldLayoutManager = fieldLayoutManager;
		this.permissionSchemeManager = permissionSchemeManager;
		this.notificationSchemeManager = notificationSchemeManager;
		this.issueSecuritySchemeManager = issueSecuritySchemeManager;
		this.issueTypeSchemeManager = issueTypeSchemeManager;
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@PublicApi
	public CloneProjectResourceModel getProject(@QueryParam("pkey") String pkey) throws Exception
	{
		Project project = projectManager.getProjectObjByKey(pkey);
		if (project == null)
		{
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
	public String deleteProject(@QueryParam("pkey") String pkey) throws Exception
	{
		Project project = projectManager.getProjectObjByKey(pkey);
		if (project == null)
		{
			throw new Exception("Project not exists or insufficient permissions");
		}
		projectManager.removeProject(project);
		return "OK";
	}
}
