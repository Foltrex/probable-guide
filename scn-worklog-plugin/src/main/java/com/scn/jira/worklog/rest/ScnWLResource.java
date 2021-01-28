package com.scn.jira.worklog.rest;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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
import com.scn.jira.worklog.remote.service.IRemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.RemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;

@Path("/scn-worklogs")
public class ScnWLResource {
	private final IRemoteScnWorklogService remoteScnWorklogService;

	@Inject
	public ScnWLResource(RemoteScnWorklogService remoteScnWorklogService) {
		this.remoteScnWorklogService = remoteScnWorklogService;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@PublicApi
	public Response getScnWorklogs(@Context HttpServletRequest request, @QueryParam("ikey") List<String> issueKeys)
			throws RemoteException {
		User user = getUser(request);
		Stream<RemoteScnWorklog> issueStream = Stream.of();
		for (String issueKey : issueKeys.stream().distinct().toArray(String[]::new))
			issueStream = Stream.concat(issueStream, Stream.of(remoteScnWorklogService.getScnWorklogs(user, issueKey)));
		RemoteScnWorklog[] scnWorklogs = issueStream.sorted((o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate()))
				.toArray(RemoteScnWorklog[]::new);
		if (scnWorklogs.length == 0)
			return Response.noContent().cacheControl(getNoCacheControl()).build();
		return Response.ok(scnWorklogs).cacheControl(getNoCacheControl()).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{ikey}")
	@PublicApi
	public Response getScnWorklogsByIssue(@Context HttpServletRequest request, @PathParam("ikey") String issueKey)
			throws RemoteException {
		User user = getUser(request);
		if (user == null)
			return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKey == null || issueKey.isEmpty())
			return Response.status(Status.BAD_REQUEST).entity("Issue key can't be NULL or Empty. ").build();
		RemoteScnWorklog[] scnWorklogs = remoteScnWorklogService.getScnWorklogs(user, issueKey);
		if (scnWorklogs == null || scnWorklogs.length == 0)
			return Response.ok("There are no SCN worklogs or you don't have permission to see them. ").status(204)
					.cacheControl(getNoCacheControl()).build();
		return Response.ok(scnWorklogs).cacheControl(getNoCacheControl()).build();
	}

	private User getUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDirectoryUser();
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}
