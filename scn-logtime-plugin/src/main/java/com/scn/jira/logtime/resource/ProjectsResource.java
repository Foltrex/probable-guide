package com.scn.jira.logtime.resource;

import java.util.Collection;
import java.util.LinkedList;

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

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
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
    private UserManager userManager;
    private PermissionManager permissionManager;
    private UserUtil userUtil;

    /**
     * Constructor.
     * @param userManager a SAL object used to find remote usernames in
     * Atlassian products
     * @param userUtil a JIRA object to resolve usernames to JIRA's internal
     * {@code com.opensymphony.os.User} objects
     * @param permissionManager the JIRA object which manages permissions
     * for users and projects
     */
    @Inject
    public ProjectsResource(@ComponentImport UserManager userManager, @ComponentImport UserUtil userUtil,
                            @ComponentImport PermissionManager permissionManager)
    {
        this.userManager = userManager;
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
        String username = userManager.getRemoteUsername(request);

        // get the corresponding com.opensymphony.os.User object for
        // the request
        ApplicationUser user = userUtil.getUser(username);

        // retrieve all objects for projects this user has permission to browse
        Collection<Project> projects =
                permissionManager.getProjects(Permissions.BROWSE, user);

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
