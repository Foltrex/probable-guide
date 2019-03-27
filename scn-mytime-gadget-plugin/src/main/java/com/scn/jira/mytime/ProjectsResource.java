package com.scn.jira.mytime;

import java.util.Collection;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/projects")
@Named
public class ProjectsResource {
	private PermissionManager permissionManager;
	private UserUtil userUtil;
	
	/**
	 * Constructor.
	 * 
	 * @param userManager
	 *            a SAL object used to find remote usernames in Atlassian
	 *            products
	 * @param userUtil
	 *            a JIRA object to resolve usernames to JIRA's internal
	 *            {@code com.opensymphony.os.User} objects
	 * @param permissionManager
	 *            the JIRA object which manages permissions for users and
	 *            projects
	 */
	@Inject
	public ProjectsResource(UserUtil userUtil,
							@ComponentImport PermissionManager permissionManager) {
		this.userUtil = userUtil;
		this.permissionManager = permissionManager;
	}
	
	/**
	 * Returns the list of projects browsable by the user in the specified
	 * request.
	 * 
	 * @param request
	 *            the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getProjects(@Context HttpServletRequest request) {
		// the request was automatically injected with @Context, so
		// we can use SAL to extract the username from it
		String username = request.getRemoteUser();
		
		// get the corresponding com.opensymphony.os.User object for
		// the request
		ApplicationUser user = userUtil.getUser(username);
		
		// retrieve all objects for projects this user has permission to browse
		Collection<Project> projects = permissionManager.getProjects(Permissions.BROWSE, user); //TODO тут было getProjectObjects
		
		// convert the project objects to ProjectRepresentations
		Collection<ProjectRepresentation> projectRepresentations = new LinkedList<ProjectRepresentation>();
		for (Project project : projects) {
			projectRepresentations.add(new ProjectRepresentation(project));
		}
		ProjectsRepresentation allProjects = new ProjectsRepresentation(projectRepresentations);
		
		// return the project representations. JAXB will handle the conversion
		// to XML or JSON.
		return Response.ok(allProjects).build();
	}
}
