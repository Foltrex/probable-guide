package com.scn.jira.mytime;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/projects")
@Named
@RequiredArgsConstructor
public class ProjectsResource {
    private final PermissionManager permissionManager;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@Context HttpServletRequest request) {
        // get the corresponding com.opensymphony.os.User object for
        // the request
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        // retrieve all objects for projects this user has permission to browse
        Collection<Project> projects = permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user);

        // convert the project objects to ProjectRepresentations
        Collection<ProjectRepresentation> projectRepresentations = new LinkedList<>();
        for (Project project : projects) {
            projectRepresentations.add(new ProjectRepresentation(project));
        }
        ProjectsRepresentation allProjects = new ProjectsRepresentation(projectRepresentations);

        // return the project representations. JAXB will handle the conversion
        // to XML or JSON.
        return Response.ok(allProjects).build();
    }
}
