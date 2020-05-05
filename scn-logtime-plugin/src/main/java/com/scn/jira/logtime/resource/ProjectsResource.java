package com.scn.jira.logtime.resource;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.representation.ProjectRepresentation;
import com.scn.jira.logtime.representation.ProjectsRepresentation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Named
@Path("/projectsuser")
public class ProjectsResource extends BaseResource {
    private PermissionManager permissionManager;

    @Inject
    public ProjectsResource(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@Context HttpServletRequest request) {
        // get the corresponding com.opensymphony.os.User object for
        // the request
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        // retrieve all objects for projects this user has permission to browse
        List<Project> projects = new ArrayList<>(
            permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user));

        projects.sort(Comparator.comparing(pr -> pr.getName().toUpperCase()));

        // convert the project objects to ProjectRepresentations
        Collection<ProjectRepresentation> projectRepresentations = new LinkedList<>();
        for (Project project : projects) {
            projectRepresentations.add(new ProjectRepresentation(project));
        }
        ProjectsRepresentation allProjects = new ProjectsRepresentation(projectRepresentations);

        return Response.ok(allProjects).build();
    }

    @GET
    @Path("/validate")
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validate(@QueryParam("project") String projects) {
        return Response.ok().cacheControl(getNoCacheControl()).build();
    }
}
