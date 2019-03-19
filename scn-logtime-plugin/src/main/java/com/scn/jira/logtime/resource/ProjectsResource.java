package com.scn.jira.logtime.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.representation.ProjectRepresentation;
import com.scn.jira.logtime.representation.ProjectsRepresentation;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/projectsuser")
public class ProjectsResource
{
    private PermissionManager permissionManager;
    private UserUtil userUtil;

    /**
     * Constructor.
     * Atlassian products
     * @param userUtil a JIRA object to resolve usernames to JIRA's internal
     * {@code com.opensymphony.os.User} objects
     * @param permissionManager the JIRA object which manages permissions
     * for users and projects
     */
    @Inject
    public ProjectsResource(@ComponentImport UserUtil userUtil,
                            @ComponentImport PermissionManager permissionManager)
    {
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns the list of projects browsable by the user in the specified
     * request.
     * @param request the context-injected {@code HttpServletRequest}
     * @return a {@code Response} with the marshalled projects
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@Context HttpServletRequest request)
    {
        // the request was automatically injected with @Context, so
        // we can use SAL to extract the username from it
        String username = request.getRemoteUser();

        // get the corresponding com.opensymphony.os.User object for
        // the request
        ApplicationUser user = userUtil.getUser(username);

        // retrieve all objects for projects this user has permission to browse
        List<Project> projects =
                new ArrayList<Project>(permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user));

        Collections.sort(projects, new Comparator<Project>() {
            public int compare(Project pr1, Project pr2) {
                return pr1.getName().toUpperCase().compareTo(pr2.getName().toUpperCase());
            }
        });

        // convert the project objects to ProjectRepresentations
        Collection<ProjectRepresentation> projectRepresentations =
                new LinkedList<ProjectRepresentation>();
        for (Project project : projects)
        {
            projectRepresentations.add(new ProjectRepresentation(project));
        }
        ProjectsRepresentation allProjects =
                new ProjectsRepresentation(projectRepresentations);
     
        return Response.ok(allProjects).build();
    }
    
    private CacheControl getNoCacheControl()
	{
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
	
	@GET
	@Path("/validate")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response validate(@QueryParam("project") String projects)
	{
		
		return Response.ok().cacheControl(getNoCacheControl()).build();
		
	}

}
