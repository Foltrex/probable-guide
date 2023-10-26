package com.scn.jira.worklog.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.impl.domain.dto.ScnWorklogDto;
import com.scn.jira.worklog.impl.domain.dto.mapper.ScnWorklogMapper;
import com.scn.jira.worklog.remote.service.IRemoteScnWorklogService;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/scn-worklogs")
@RequiredArgsConstructor
@Log4j
public class ScnWLResource {
    private static final char WEEK = 'w';
    private static final char DAY = 'd';
    private static final char HOUR = 'h';
    private static final char MINUTE = 'm';
    private static final char SECOND = 's';

    private final IRemoteScnWorklogService remoteScnWorklogService;
    private final JiraAuthenticationContext authenticationContext;
    private final IScnProjectSettingsManager projectSettingsManager;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final IScnWorklogService scnDefaultWorklogService;
    private final UserManager userManager;
    private final ExtendedConstantsManager constantsManager;
    private final IGlobalSettingsManager scnGlobalPermissionManager;
    private final ScnWorklogMapper scnWorklogMapper;

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
    @Path("/issue/{issueIdOrKey}/worklog")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PublicApi
    public Response getIssueWorklog(@Context HttpServletRequest request, @PathParam("issueIdOrKey") String issueIdOrKey) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        if (scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user)) {
            JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user);
            Issue issue = issueManager.getIssueObject(issueIdOrKey);
            List<ScnWorklogDto> scnWorklogDtos = scnDefaultWorklogService.getByIssueVisibleToUser(jiraServiceContext, issue)
                .stream()
                .map(scnWorklogMapper::mapToDto)
                .collect(Collectors.toList());
            return Response.ok(scnWorklogDtos).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/issue/{issueIdOrKey}/worklog/{id}")
    @PublicApi
    public Response deleteWorklog(
        @PathParam("issueIdOrKey") String issueIdOrKey,
        @PathParam("id") Long id,
        @QueryParam("adjustEstimate") @DefaultValue("") String adjustEstimate,
        @QueryParam("newEstimate") String newEstimate,
        @QueryParam("increaseBy") String increaseBy
    ) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user);
        IScnWorklog scnWorklog = scnDefaultWorklogService.getById(jiraServiceContext, id);
//        wl auto copy ??
        boolean isLinkedWorklog = true;
        boolean dispatchEvent = false;

        switch (adjustEstimate) {
            case "new": {
                if (newEstimate == null) {
                    throw new IllegalArgumentException();
                }

                long parsedTime = parseTime(newEstimate);
                IScnWorklogService.WorklogNewEstimateResult result = new IScnWorklogService.WorklogNewEstimateResult(scnWorklog, parsedTime);
                scnDefaultWorklogService.deleteWithNewRemainingEstimate(jiraServiceContext, result, dispatchEvent, isLinkedWorklog);
                break;
            }
            case "leave": {
                scnDefaultWorklogService.deleteAndRetainRemainingEstimate(jiraServiceContext, scnWorklog, dispatchEvent, isLinkedWorklog);
                break;
            }
            case "manual": {
                if (increaseBy == null) {
                    throw new IllegalArgumentException();
                }

                long parsedTime = parseTime(increaseBy);
                IScnWorklogService.WorklogAdjustmentAmountResult result = new IScnWorklogService.WorklogAdjustmentAmountResult(scnWorklog, parsedTime);
                scnDefaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, result, dispatchEvent, isLinkedWorklog);
                break;
            }
            default: {
                scnDefaultWorklogService.deleteAndAutoAdjustRemainingEstimate(jiraServiceContext, scnWorklog, dispatchEvent, isLinkedWorklog);
                break;
            }
        }

        return Response.status(Status.NO_CONTENT).build();
    }

    private long parseTime(String time) {
        char unit = time.charAt(time.length() - 1);
        int value = Integer.parseInt(time.substring(0, time.length() - 1));

        switch (unit) {
            case WEEK:
                return value * 5L * 8L * 3600L;
            case DAY:
                return value * 8L * 3600L;
            case HOUR:
                return value * 3600L;
            case MINUTE:
                return value * 60L;
            case SECOND:
                return value;
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }
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
