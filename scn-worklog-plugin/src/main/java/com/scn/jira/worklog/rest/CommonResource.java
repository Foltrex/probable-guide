package com.scn.jira.worklog.rest;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.remote.service.IRemoteScnExtIssueService;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;

@Path("/")
public class CommonResource {
	private final IRemoteScnExtIssueService remoteScnExtIssueService;
	private final IGlobalSettingsManager settingsManager;
	private final ProjectManager projectManager;
	private final IScnProjectSettingsManager projectSettingManager;
	private final GlobalPermissionManager permissionManager;

	@Inject
	public CommonResource(IRemoteScnExtIssueService remoteScnExtIssueService, IGlobalSettingsManager settingsManager,
			ProjectManager projectManager, IScnProjectSettingsManager projectSettingManager,
			GlobalPermissionManager permissionManager) {
		this.remoteScnExtIssueService = remoteScnExtIssueService;
		this.settingsManager = settingsManager;
		this.projectManager = projectManager;
		this.projectSettingManager = projectSettingManager;
		this.permissionManager = permissionManager;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-ext-issues")
	@PublicApi
	public Response getScnExtendedIssues(@Context HttpServletRequest request,
			@QueryParam("ikey") List<String> issueKeys) throws RemoteException {
		ApplicationUser user = getApplicationUser(request);
		if (user == null)
			return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKeys == null || issueKeys.isEmpty())
			return Response.status(Status.BAD_REQUEST).entity("Issue keys can't be NULL or Empty. ").build();
		RemoteScnExtIssue[] scnExtIssues = remoteScnExtIssueService.getScnExtIssues(user.getDirectoryUser(), issueKeys);
		if (scnExtIssues == null || scnExtIssues.length == 0)
			return Response.ok("There are no SCN Extended Issues or you don't have permission to see them. ")
					.cacheControl(getNoCacheControl()).build();
		return Response.ok(scnExtIssues).cacheControl(getNoCacheControl()).build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globalsettings/moveGroup")
	@PublicApi
	public Response moveGlobalSecurityGroup(@Context HttpServletRequest request,
			@FormParam("operation") String operation, @FormParam("groupnames") List<String> groupnames)
			throws RemoteException {
		// TODO: Admin user required???
		ApplicationUser user = getApplicationUser(request);
		if (user == null)
			return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (operation.equals("add")) {
			settingsManager.addGroups(groupnames);
		} else if (operation.equals("remove")) {
			settingsManager.removeGroups(groupnames);
		} else
			return Response.status(Status.BAD_REQUEST).entity("Unknown operation code. ").build();
		return Response.ok().cacheControl(getNoCacheControl()).build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/wlworklogblockingdate/{projectKey}")
	@PublicApi
	public Response setWLWorklogBlockingDate(@Context HttpServletRequest request,
			@PathParam("projectKey") String projectKey, @QueryParam("date") String date) throws RemoteException {
		if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, getApplicationUser(request)))
			return Response.status(Status.BAD_REQUEST).entity("Don't have permission.").build();
		try {
			projectSettingManager.setWLWorklogBlockingDate(projectManager.getProjectObjByKey(projectKey).getId(),
					new SimpleDateFormat("yyyyMMddhhmmss").parse(date));
		} catch (PropertyException | ParseException e) {
			return Response.status(Status.BAD_REQUEST).entity("Incorrect data format.").build();
		}
		return Response.ok().cacheControl(getNoCacheControl()).entity("OK").build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/wlblockingdate/{projectKey}")
	@PublicApi
	public Response setWLBlockingDate(@Context HttpServletRequest request, @PathParam("projectKey") String projectKey,
			@QueryParam("date") String date) throws RemoteException {
		if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, getApplicationUser(request)))
			return Response.status(Status.BAD_REQUEST).entity("Don't have permission.").build();
		try {
			projectSettingManager.setWLBlockingDate(projectManager.getProjectObjByKey(projectKey).getId(),
					new SimpleDateFormat("yyyyMMddhhmmss").parse(date));
		} catch (PropertyException | ParseException e) {
			return Response.status(Status.BAD_REQUEST).entity("Incorrect data format.").build();
		}
		return Response.ok().cacheControl(getNoCacheControl()).entity("OK").build();
	}

	private ApplicationUser getApplicationUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}
