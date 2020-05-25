package com.scn.cloneproject.rest;

import java.util.List;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

@Named
@Path("/doclone")
public class CloneProjectResource {
    private final ProjectService projectService;
    private final AvatarManager avatarManager;
    private final ProjectManager projectManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final UserManager userManager;

    public CloneProjectResource(ProjectService projectService, ProjectManager projectManager,
                                PermissionSchemeManager permissionSchemeManager, NotificationSchemeManager notificationSchemeManager,
                                IssueSecuritySchemeManager issueSecuritySchemeManager, WorkflowSchemeManager workflowSchemeManager,
                                FieldLayoutManager fieldLayoutManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
                                AvatarManager avatarManager, UserManager userManager) {
        this.projectService = projectService;
        this.projectManager = projectManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
    }

    @POST
    @Produces("text/plain")
    @PublicApi
    public String DoClonePost(@QueryParam("pkey") String pkey, @QueryParam("pname") String pname,
                              @QueryParam("templatekey") String templatekey, @QueryParam("lead") String lead,
                              @QueryParam("descr") String descr, @QueryParam("url") String url) {
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        if (!projectService.getProjectByKey(currentUser, templatekey).isValid()) {
            return "Template project does not exist or no permissions";
        }

        Project sourceProject = projectManager.getProjectObjByKey(templatekey);
        Avatar srcProjectAvatar = sourceProject.getAvatar();
        final ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(currentUser,
            (new ProjectCreationData.Builder()
                .fromExistingProject(sourceProject,
                    new ProjectCreationData.Builder().withName(pname).withKey(pkey).withDescription(descr)
                        .withLead(userManager.getUserByKey(lead)).withUrl(url)
                        .withAssigneeType(sourceProject.getAssigneeType())
                        .withAvatarId(srcProjectAvatar.isSystemAvatar() ? srcProjectAvatar.getId()
                            : avatarManager.getDefaultAvatarId(IconType.PROJECT_ICON_TYPE))
                        .build())
                .build()));

        if (!result.isValid()) {
            return String.format("Failed to create a project. Errors: %s", result.getErrorCollection().toString());
        }

        // create a project (key, name, lead, descr, url, asignee type, avatar
        final Project newprojectObj = projectService.createProject(result);
        // project category
        projectManager.setProjectCategory(newprojectObj, projectManager.getProjectCategoryForProject(sourceProject));

        // workflow scheme
        if (workflowSchemeManager.getSchemeFor(sourceProject) != workflowSchemeManager.getDefaultSchemeObject()) {
            workflowSchemeManager.addSchemeToProject(newprojectObj, workflowSchemeManager.getSchemeFor(sourceProject));
        }
        // issue types scheme
        com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager itsmanager = ComponentAccessor
            .getIssueTypeSchemeManager();

        com.atlassian.jira.issue.fields.config.FieldConfigScheme issueTypeScheme = itsmanager
            .getConfigScheme(sourceProject);

        if (issueTypeScheme != itsmanager.getDefaultIssueTypeScheme()) {
            List<Long> projectIDs = issueTypeScheme.getAssociatedProjectIds();
            projectIDs.add(newprojectObj.getId());
            List<com.atlassian.jira.issue.context.JiraContextNode> contexts = com.atlassian.jira.issue.customfields.CustomFieldUtils
                .buildJiraIssueContexts(false, projectIDs.toArray(new Long[projectIDs.size()]), projectManager);

            FieldConfigSchemeManager fcsmanager = (FieldConfigSchemeManager) ComponentAccessor
                .getComponentOfType(com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager.class);
            fcsmanager.updateFieldConfigScheme(issueTypeScheme, contexts, issueTypeScheme.getField());

            ComponentAccessor.getFieldManager().refresh();
        }
        // issue type screen scheme
        issueTypeScreenSchemeManager.addSchemeAssociation(newprojectObj,
            issueTypeScreenSchemeManager.getIssueTypeScreenScheme(sourceProject));
        // field configuration scheme
        FieldConfigurationScheme srcProjectFieldLayout = fieldLayoutManager.getFieldConfigurationScheme(sourceProject);
        if (srcProjectFieldLayout != null) {
            fieldLayoutManager.addSchemeAssociation(newprojectObj, srcProjectFieldLayout.getId());
        }
        // permissions scheme, notifications scheme, issue security scheme
        final ProjectService.UpdateProjectSchemesValidationResult schemesResult = projectService
            .validateUpdateProjectSchemes(currentUser,
                permissionSchemeManager.getSchemeFor(sourceProject) == null ? null
                    : permissionSchemeManager.getSchemeFor(sourceProject).getId(),
                notificationSchemeManager.getSchemeFor(sourceProject) == null ? null
                    : notificationSchemeManager.getSchemeFor(sourceProject).getId(),
                issueSecuritySchemeManager.getSchemeFor(sourceProject) == null ? null
                    : issueSecuritySchemeManager.getSchemeFor(sourceProject).getId());

        projectService.updateProjectSchemes(schemesResult, newprojectObj);

        return "OK";
    }

    @GET
    @AnonymousAllowed
    @Produces("text/plain")
    public String DoCloneGet() {
        return "Do POST instead of GET";
    }
}
