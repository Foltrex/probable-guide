package com.scn.jira.worklog.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.remote.service.IRemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Path("/scn-worklogs")
@RequiredArgsConstructor
public class ScnWLResource {
    private final IRemoteScnWorklogService remoteScnWorklogService;
    private final JiraAuthenticationContext authenticationContext;
    private final IScnProjectSettingsManager projectSettingsManager;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final IScnWorklogService scnDefaultWorklogService;
    private final UserManager userManager;
    private final ExtendedConstantsManager constantsManager;

    @POST
    @Path("/issue/{issueKey}/worklog")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PublicApi
    public Response createScnWorklog(@PathParam("issueKey") String issueKey, ScnWorklogRequest request) {
        Issue issue;
        ApplicationUser author;
        if (StringUtils.isBlank(issueKey) || (issue = issueManager.getIssueObject(issueKey)) == null) {
            return Response.status(Status.BAD_REQUEST).entity("Cannot find issue by specified key").cacheControl(getNoCacheControl()).build();
        } else if (StringUtils.isBlank(request.getAuthorKey()) || (author = userManager.getUserByName(request.getAuthorKey())) == null) {
            return Response.status(Status.BAD_REQUEST).entity("The property 'authorKey' must be specified with username and valid").cacheControl(getNoCacheControl()).build();
        } else if (StringUtils.isBlank(request.getWorklogTypeId()) || constantsManager.getWorklogType(request.getWorklogTypeId()) == null) {
            return Response.status(Status.BAD_REQUEST).entity("The property 'worklogTypeId' must be specified and valid").cacheControl(getNoCacheControl()).build();
        } else if (request.getStarted() == null) {
            return Response.status(Status.BAD_REQUEST).entity("The property 'started' must be specified").cacheControl(getNoCacheControl()).build();
        } else if (request.getTimeSpentSeconds() == null || request.getTimeSpentSeconds() <= 0L) {
            return Response.status(Status.BAD_REQUEST).entity("The property 'timeSpentSeconds' must be specified with value greater than or equal to 0").cacheControl(getNoCacheControl()).build();
        }
        IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, null, author.getKey(),
            request.getComment(), request.started, null, null, request.getTimeSpentSeconds(), request.getWorklogTypeId());
        boolean isAutoCopy = isWlAutoCopy(request, issue);
        JiraServiceContextImpl context = new JiraServiceContextImpl(authenticationContext.getLoggedInUser());
        scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(context, worklog, true, isAutoCopy);
        if (context.getErrorCollection().hasAnyErrors()) {
            return Response.status(Status.FORBIDDEN).entity(String.join(SystemUtils.LINE_SEPARATOR, context.getErrorCollection().getErrorMessages())).cacheControl(getNoCacheControl()).build();
        }
        return Response.status(Status.CREATED).cacheControl(getNoCacheControl()).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @PublicApi
    public Response getScnWorklogs(@Context HttpServletRequest request, @QueryParam("ikey") List<String> issueKeys)
        throws RemoteException {
        User user = authenticationContext.getLoggedInUser().getDirectoryUser();
        Stream<RemoteScnWorklog> issueStream = Stream.of();
        for (String issueKey : issueKeys.stream().distinct().toArray(String[]::new))
            issueStream = Stream.concat(issueStream, Stream.of(remoteScnWorklogService.getScnWorklogs(user, issueKey)));
        RemoteScnWorklog[] scnWorklogs = issueStream.sorted(Comparator.comparing(RemoteScnWorklog::getStartDate))
            .toArray(RemoteScnWorklog[]::new);
        if (scnWorklogs.length == 0)
            return Response.noContent().cacheControl(getNoCacheControl()).build();
        return Response.ok(scnWorklogs).cacheControl(getNoCacheControl()).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{ikey}")
    @PublicApi
    public Response getScnWorklogsByIssue(@Context HttpServletRequest request, @PathParam("ikey") String issueKey)
        throws RemoteException {
        User user = authenticationContext.getLoggedInUser().getDirectoryUser();
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

    private CacheControl getNoCacheControl() {
        CacheControl noCache = new CacheControl();
        noCache.setNoCache(true);
        return noCache;
    }

    private boolean isWlAutoCopy(ScnWorklogRequest request, Issue issue) {
        return projectSettingsManager.isWLAutoCopyEnabled(issue.getProjectId())
            && (request.getWorklogTypeId() == null ?
            projectSettingsManager.isUnspecifiedWLTypeAutoCopyEnabled(issue.getProjectId())
            : projectSettingsManager.getWorklogTypes(issue.getProjectId()).stream()
            .anyMatch(worklogType -> worklogType.getId().equals(request.getWorklogTypeId()))
        );
    }

    @Data
    @JsonAutoDetect
    private static class ScnWorklogRequest {
        private String authorKey;
        private String worklogTypeId;
        private Date started;
        private String comment;
        private Long timeSpentSeconds;
    }
}
