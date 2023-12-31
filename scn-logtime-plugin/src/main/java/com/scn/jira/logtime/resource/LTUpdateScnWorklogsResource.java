package com.scn.jira.logtime.resource;

import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Named
@Path("/updateScnWorklogs")
@RequiredArgsConstructor
public class LTUpdateScnWorklogsResource extends BaseResource {
    private final IssueManager issueManager;
    private final IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
    private final IScnWorklogService scnWorklogService;
    private final IScnProjectSettingsManager projectSettignsManager;

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getWorklogForUpdate(@Context HttpServletRequest request,
                                        @QueryParam("wlsToSave") List<String> wlsToSave, @QueryParam("issueId") String issueId) {
        if (wlsToSave == null)
            return Response.ok("NOTHING TO SAVE").build();

        Issue issue = issueManager.getIssueObject(Long.parseLong(issueId));
        ApplicationUser appUser = getLoggedInUser();
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(appUser);
        Long projectId = Objects.requireNonNull(issue.getProjectObject()).getId();
        if (!scnWorklogService.hasPermissionToCreate(serviceContext, issue, null)) {
            return Response.serverError()
                .entity(new ErrorEntity(
                    ErrorEntity.ErrorReason.APPLICATION_PERMISSION_DENIED,
                    serviceContext.getErrorCollection().getErrorMessages().stream().findFirst().orElse("Permission Denied (insufficient rights).")))
                .status(Response.Status.FORBIDDEN)
                .build();
        }

        Map<String, String> wlToCreate = wlsToSave.stream()
            .collect(Collectors.toMap(v -> getWlIdFromRequestParameter(v, 2), v -> v, (p, n) -> n)).entrySet()
            .stream().filter(e -> !getWlIdFromRequestParameter(e.getValue(), 0).isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (String wlKey : wlToCreate.keySet()) {
            String wlString = wlToCreate.get(wlKey);
            String time = getWlIdFromRequestParameter(wlString, 0);
            String comment = String.valueOf(getWlIdFromRequestParameter(wlString, 1));
            String date = getWlIdFromRequestParameter(wlString, 2);
            String userKey = getWlIdFromRequestParameter(wlString, 3).equals("") ? null
                : getWlIdFromRequestParameter(wlString, 3);

            String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(wlString, 4).equals("") ? ""
                : Integer.valueOf(getWlIdFromRequestParameter(wlString, 4)));

            boolean isValidWLType = !(iScnWorklogLogtimeStore.isWLTypeRequired(projectId) &&
                (worklogTypeId == null || worklogTypeId.isEmpty() || worklogTypeId.equals("0")));
            if (!isValidWLType) {
                return Response.serverError()
                    .entity(new ErrorEntity(
                        ErrorEntity.ErrorReason.ILLEGAL_ARGUMENT, "Type has to be set"))
                    .status(Response.Status.FORBIDDEN)
                    .build();
            } else if (projectSettignsManager.isWLCommentRequired(projectId) && StringUtils.isBlank(comment)) {
                return Response.serverError()
                    .entity(new ErrorEntity(
                        ErrorEntity.ErrorReason.ILLEGAL_ARGUMENT, "Comment has to be set"))
                    .status(Response.Status.FORBIDDEN)
                    .build();
            }

            userKey = userKey != null ? userKey : "";

            createWorklogS(time, comment, date, userKey, worklogTypeId, issueId);
        }

        LTMessages message = new LTMessages("DONE!");
        message.setMessage(issue != null ? String.valueOf(Objects.requireNonNull(issue.getProjectObject()).getId()) : "");
        return Response.ok(message).build();
    }

    private void createWorklogS(String time, String comment, String date, String userKey, String worklogTypeId,
                                String issueId) {
        Long timeSpent = 0L;
        if (TextFormatUtil.matchesPattern1(time)) {
            timeSpent = TextFormatUtil.stringToTime(time);
        } else {
            if (TextFormatUtil.matchesPattern2(time)) {
                timeSpent = TextFormatUtil.string2ToTime(time);
            } else {
                if (TextFormatUtil.matchesPattern3(time)) {
                    timeSpent = TextFormatUtil.string3ToTime(time);
                }
            }
        }
        Date day = DateUtils.stringToDate(date);
        if (day != null)
            if (time != null && !time.equals("00:00") && !time.equals("0") && !time.equals(""))
                // Long timeSpent = TextFormatUtil.stringToTime(time);
                if (timeSpent != 0)
                    // Here we will create a worklog
                    if (issueId != null && worklogTypeId != null)
                        createScnWorklog(Long.parseLong(issueId), worklogTypeId, timeSpent,
                            comment != null ? comment : "", userKey, day, worklogTypeId);
    }

    private void createScnWorklog(Long issueId, String _worklogType, Long _timeSpent,
                                  String _comment, String authorKey, Date date, String worklogTypeId) {
        iScnWorklogLogtimeStore.createScnWorklog(issueId, _worklogType, _timeSpent, _comment, authorKey, date,
            worklogTypeId);
    }
}
