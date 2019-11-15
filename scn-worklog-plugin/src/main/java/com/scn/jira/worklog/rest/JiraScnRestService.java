package com.scn.jira.worklog.rest;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.crowd.integration.rest.entity.ErrorEntity.ErrorReason;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
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
	private final ProjectManager projectManager;
	private final IScnProjectSettingsManager projectSettingManager;
	private final GlobalPermissionManager permissionManager;
	private final IScnExtendedIssueStore ofBizExtIssueStore;
	private final IssueManager issueManager;

	@Inject
	public JiraScnRestService(RemoteScnWorklogService remoteScnWorklogService,
			IRemoteScnExtIssueService remoteScnExtIssueService, IGlobalSettingsManager settingsManager,
			ProjectManager projectManager, IScnProjectSettingsManager projectSettingManager,
			GlobalPermissionManager permissionManager, IScnExtendedIssueStore ofBizExtIssueStore,
			@ComponentImport IssueManager issueManager) {
		this.remoteScnWorklogService = remoteScnWorklogService;
		this.remoteScnExtIssueService = remoteScnExtIssueService;
		this.settingsManager = settingsManager;
		this.projectManager = projectManager;
		this.projectSettingManager = projectSettingManager;
		this.permissionManager = permissionManager;
		this.ofBizExtIssueStore = ofBizExtIssueStore;
		this.issueManager = issueManager;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-worklogs")
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
	@Path("/scn-worklogs/{ikey}")
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

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/scn-ext-issues")
	@PublicApi
	public Response getScnExtendedIssues(@Context HttpServletRequest request,
			@QueryParam("ikey") List<String> issueKeys) throws RemoteException {
		User user = getUser(request);
		if (user == null)
			return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
		if (issueKeys == null || issueKeys.isEmpty())
			return Response.status(Status.BAD_REQUEST).entity("Issue keys can't be NULL or Empty. ").build();
		RemoteScnExtIssue[] scnExtIssues = remoteScnExtIssueService.getScnExtIssues(user, issueKeys);
		if (scnExtIssues == null || scnExtIssues.length == 0)
			return Response.ok("There are no SCN Extended Issues or you don't have permission to see them. ")
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
	@Path("/scn-ext-issue/{ikey}/")
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
			// TODO. To be continued.
		}
		RemoteScnExtIssue scnExtIssue = remoteScnExtIssueService.getScnExtIssue(appUser.getDirectoryUser(), issueKey);
		return Response.ok(scnExtIssue).cacheControl(getNoCacheControl()).build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globalsettings/moveGroup/")
	@PublicApi
	public Response moveGlobalSecurityGroup(@Context HttpServletRequest request,
			@FormParam("operation") String operation, @FormParam("groupnames") List<String> groupnames)
			throws RemoteException {
		// TODO: Admin user required???
		User user = getUser(request);
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
	@Path("/worklogblockingdate/{projectKey}")
	@PublicApi
	public Response setWorklogBlockingDate(@Context HttpServletRequest request, @PathParam("pkey") String projectKey,
			@QueryParam("date") String date) throws RemoteException {
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
