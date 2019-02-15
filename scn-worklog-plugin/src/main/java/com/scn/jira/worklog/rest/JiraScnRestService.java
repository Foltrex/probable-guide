package com.scn.jira.worklog.rest;

import java.rmi.RemoteException;
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
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.remote.service.IRemoteScnExtIssueService;
import com.scn.jira.worklog.remote.service.IRemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.RemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;

@Path("/")
public class JiraScnRestService {

	private final IRemoteScnWorklogService remoteScnWorklogService;
	private final IRemoteScnExtIssueService remoteScnExtIssueService;
	private final IGlobalSettingsManager settingsManager;

	@Inject
	public JiraScnRestService(RemoteScnWorklogService remoteScnWorklogService,
			IRemoteScnExtIssueService remoteScnExtIssueService, IGlobalSettingsManager settingsManager) {
		this.remoteScnWorklogService = remoteScnWorklogService;
		this.remoteScnExtIssueService = remoteScnExtIssueService;
		this.settingsManager = settingsManager;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-worklogs/{ikey}")
	@PublicApi
	public Response getScnWorklogs(@Context HttpServletRequest request, @PathParam("ikey") String issueKey)
			throws RemoteException {
		User user = getUser(request);
		if (user == null) return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKey == null || issueKey.isEmpty()) return Response.status(Status.BAD_REQUEST)
				.entity("Issue key can't be NULL or Empty. ").build();
		RemoteScnWorklog[] scnWorklogs = remoteScnWorklogService.getScnWorklogs(user, issueKey);
		if (scnWorklogs == null || scnWorklogs.length == 0) return Response
				.ok("There are no SCN worklogs or you don't have permission to see them. ").cacheControl(getNoCacheControl())
				.build();
		return Response.ok(scnWorklogs).cacheControl(getNoCacheControl()).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-ext-issues")
	@PublicApi
	public Response getScnExtendedIssues(@Context HttpServletRequest request, @QueryParam("ikey") List<String> issueKeys)
			throws RemoteException {
		User user = getUser(request);
		if (user == null) return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKeys == null || issueKeys.isEmpty()) return Response.status(Status.BAD_REQUEST)
				.entity("Issue keys can't be NULL or Empty. ").build();
		RemoteScnExtIssue[] scnExtIssues = remoteScnExtIssueService.getScnExtIssues(user, issueKeys);
		if (scnExtIssues == null || scnExtIssues.length == 0) return Response
				.ok("There are no SCN Extended Issues or you don't have permission to see them. ")
				.cacheControl(getNoCacheControl()).build();
		return Response.ok(scnExtIssues).cacheControl(getNoCacheControl()).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-ext-issue/{ikey}")
	@PublicApi
	public Response getScnExtendedIssue(@Context HttpServletRequest request, @PathParam("ikey") String issueKey)
			throws RemoteException {
		User user = getUser(request);
		if (user == null) return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKey == null || issueKey.isEmpty()) return Response.status(Status.BAD_REQUEST)
				.entity("Issue key can't be NULL or Empty. ").build();
		RemoteScnExtIssue scnExtIssue = remoteScnExtIssueService.getScnExtIssue(user, issueKey);
		if (scnExtIssue == null) return Response.ok("There is such SCN Extended Issues or you don't have permission to see it. ")
				.cacheControl(getNoCacheControl()).build();
		return Response.ok(scnExtIssue).cacheControl(getNoCacheControl()).build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globalsettings/moveGroup/")
	@PublicApi
	public Response moveGlobalSecurityGroup(@Context HttpServletRequest request, @FormParam("operation") String operation,
			@FormParam("groupnames") List<String> groupnames) throws RemoteException {
		// TODO: Admin user required???
		User user = getUser(request);
		if (user == null) return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (operation.equals("add")) {
			settingsManager.addGroups(groupnames);
		} else if (operation.equals("remove")) {
			settingsManager.removeGroups(groupnames);
		} else return Response.status(Status.BAD_REQUEST).entity("Unknown operation code. ").build();
		return Response.ok().cacheControl(getNoCacheControl()).build();
	}

	private User getUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDirectoryUser(); // TODO should be re-factored
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}

}
