package com.scn.jira.worklog.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.crowd.integration.rest.entity.ErrorEntity.ErrorReason;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;
import com.scn.jira.worklog.remote.service.IRemoteScnExtIssueService;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.rmi.RemoteException;

@Path("/scn-ext-issue")
public class ExtIssueResource {
	private final IRemoteScnExtIssueService remoteScnExtIssueService;
	private final IScnExtendedIssueStore ofBizExtIssueStore;
	private final IssueManager issueManager;

	@Inject
	public ExtIssueResource(IRemoteScnExtIssueService remoteScnExtIssueService,
			IScnExtendedIssueStore ofBizExtIssueStore, IssueManager issueManager) {
		this.remoteScnExtIssueService = remoteScnExtIssueService;
		this.ofBizExtIssueStore = ofBizExtIssueStore;
		this.issueManager = issueManager;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{ikey}")
	@PublicApi
	public Response getScnExtendedIssue(@Context HttpServletRequest request, @PathParam("ikey") String issueKey)
			throws RemoteException {
		User user = getUser(request);
		if (user == null)
			return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKey == null || issueKey.isEmpty())
			return Response.status(Status.BAD_REQUEST).entity("Issue key can't be NULL or Empty. ").build();
		RemoteScnExtIssue scnExtIssue = remoteScnExtIssueService.getScnExtIssue(user, issueKey);
		if (scnExtIssue == null)
			return Response.ok("There is such SCN Extended Issues or you don't have permission to see it. ")
					.cacheControl(getNoCacheControl()).build();
		return Response.ok(scnExtIssue).cacheControl(getNoCacheControl()).build();
	}

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{ikey}")
	@PublicApi
	public Response setScnExtendedIssue(@Context HttpServletRequest request, @PathParam("ikey") String issueKey,
			@QueryParam("originalestimate") Long originalEstimate) throws RemoteException {
		if (originalEstimate == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorEntity(ErrorReason.OPERATION_FAILED, "Param 'originalEstimate' is not found."))
					.cacheControl(getNoCacheControl()).build();
		ApplicationUser appUser = getApplicationUser(request);
		Issue issue = issueManager.getIssueObject(issueKey);
		if (issue == null || !issueManager.isEditable(issue, appUser)) {
			return Response.status(Status.NOT_FOUND)
					.entity(new ErrorEntity(ErrorReason.PERMISSION_DENIED,
							"The issue is not found or you don't have permission."))
					.cacheControl(getNoCacheControl()).build();
		}
		final IScnExtendedIssue extIssue = ofBizExtIssueStore.getByIssue(issue);
		if (extIssue == null) {
			final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(issue, null, originalEstimate, originalEstimate,
					null);
			ofBizExtIssueStore.create(newExtIssue);
		} else {
			final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(issue, extIssue.getId(), originalEstimate,
					extIssue.getTimeSpent() == null ? originalEstimate
							: originalEstimate > extIssue.getTimeSpent() ? originalEstimate - extIssue.getTimeSpent()
									: 0L,
					extIssue.getTimeSpent());
			ofBizExtIssueStore.update(newExtIssue);
		}
		RemoteScnExtIssue scnExtIssue = remoteScnExtIssueService.getScnExtIssue(appUser.getDirectoryUser(), issueKey);
		return Response.ok(scnExtIssue).cacheControl(getNoCacheControl()).build();
	}

	private User getUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDirectoryUser();
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
