package com.scn.jira.logtime.resource;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named
@Path("/updateScnWorklog")
public class LTUpdateScnWorklogResource extends BaseResource {
    private IssueManager issueManager;
    private IScnWorklogManager scnWorklogManager;
    private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
    private IScnWorklogService scnWorklogService;

    @Inject
    public LTUpdateScnWorklogResource(@ComponentImport JiraAuthenticationContext authenticationContext,
                                      @ComponentImport IssueManager issueManager,
                                      @ComponentImport ProjectRoleManager projectRoleManager,
                                      @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
                                      @ComponentImport DefaultScnWorklogManager scnWorklogManager,
                                      @ComponentImport ScnProjectSettingsManager projectSettignsManager,
                                      @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
                                      @ComponentImport DefaultScnWorklogService scnWorklogService) {
        this.authenticationContext = authenticationContext;
        this.issueManager = issueManager;
        this.scnWorklogManager = scnWorklogManager;
        this.scnWorklogService = scnWorklogService;
        this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager,
            overridedWorklogManager, projectSettignsManager, scnUserBlockingManager, scnWorklogService);
    }

    @GET
    @AnonymousAllowed
    @Produces({"application/json", "application/xml"})
    public Response getWorklogForUpdate(@Context HttpServletRequest request, @QueryParam("complexWLId") String complexWLId,
                                        @QueryParam("complexId2") String complexId2, @QueryParam("newValue") String newValue, @QueryParam("newWLType") String newWLType,
                                        @QueryParam("comment") String comment) {
        long issueId = getWlIdFromRequestParameter(complexWLId, 0).equals("") ? -1 : Integer.parseInt(getWlIdFromRequestParameter(complexWLId, 0));
        long worklogId = getWlIdFromRequestParameter(complexWLId, 2).equals("") ? -1 : Integer.parseInt(getWlIdFromRequestParameter(complexWLId, 2));
        String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(complexWLId, 1).equals("") ? "" : Integer
            .valueOf(getWlIdFromRequestParameter(complexWLId, 1)));
        String date = getWlIdFromRequestParameter(complexWLId, 3).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 3));
        String userCreated = getWlIdFromRequestParameter(complexWLId, 5).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 5));
        userCreated = userCreated != null ? userCreated.toLowerCase() : "";
        if (newValue != null) {
            newValue = newValue.trim();
        }

        comment = (comment != null && !comment.equals("undefined")) ? comment : null;
        String wlType = (newWLType != null && !newWLType.equals("undefined")) ? newWLType : worklogTypeId;

        boolean reloadRequired = false;
        boolean result = false;
        boolean isValueEmplty = (newValue == null || newValue.equals("00:00") || newValue.equals("0") || newValue.equals("") || newValue.equals("0h"));
        // Check what to do with the worklog
        Long timeSpent = 0L;
        if (TextFormatUtil.matchesPattern1(newValue)) {
            timeSpent = TextFormatUtil.stringToTime(newValue);
        } else {
            if (TextFormatUtil.matchesPattern2(newValue)) {
                timeSpent = TextFormatUtil.string2ToTime(newValue);
            } else {
                if (TextFormatUtil.matchesPattern3(newValue)) {
                    timeSpent = TextFormatUtil.string3ToTime(newValue);
                }
            }
        }
        Long wlId = 0L;
        Long wlIdExt = 0L;

        Issue issue = this.issueManager.getIssueObject(issueId);
        Date day = DateUtils.stringToDate(date);

        IScnWorklog scnWorklog = scnWorklogManager.getById(worklogId);
        ApplicationUser appUser = getLoggedInUser();
        boolean isBlocked = (iScnWorklogLogtimeStore.isProjectWLBlocked(Objects.requireNonNull(issue.getProjectObject()).getId(), day)
            || (iScnWorklogLogtimeStore.isWlAutoCopy(issue, wlType)
            && iScnWorklogLogtimeStore.isProjectWLWorklogBlocked(issue.getProjectObject().getId(), day)));
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(appUser);
        if (isBlocked || (worklogId != 0 && isValueEmplty && !scnWorklogService.hasPermissionToDelete(serviceContext, scnWorklog))
            || (worklogId != 0 && !isValueEmplty && !scnWorklogService.hasPermissionToUpdate(serviceContext, scnWorklog))
            || (worklogId == 0) && !isValueEmplty && !scnWorklogService.hasPermissionToCreate(serviceContext, issue)) {
            LTMessages message = new LTMessages("BLOCKED", false, false, null);
            return Response.ok(message).build();
        }

        if (worklogId == 0) {
            if (day != null) {
                if (!isValueEmplty) {
                    if (timeSpent != 0) {
                        Map<String, Object> resultMap = iScnWorklogLogtimeStore.createScnWorklogResultMap(issueId,
                            wlType, timeSpent, comment != null ? comment : "", userCreated, day, wlType);
                        result = (Boolean) resultMap.get("isAuto");
                        wlId = (Long) resultMap.get("wlId");
                        wlIdExt = (Long) resultMap.get("wlIdExt");
                        if (!wlType.equals(worklogTypeId)) {
                            reloadRequired = true;
                        }
                    }
                }
            }
        } else if (scnWorklog != null) {
            if (!isValueEmplty) {
                result = updateScnWorklog(worklogId, wlType, timeSpent, comment);
                if (!wlType.equals(worklogTypeId)) {
                    reloadRequired = true;
                }
                wlId = scnWorklog.getId();
                Worklog ext = scnWorklog.getLinkedWorklog();
                if (ext != null) {
                    wlIdExt = ext.getId();
                }
            } else {
                result = deleteScnWorklog(worklogId);
            }
        }
        String complexWLIdnew = changeWlIdFromRequestParameter(complexWLId, wlId),
            complexWLIdExtNew = changeWlIdFromRequestParameter(complexId2, wlIdExt);
        LTMessages message = new LTMessages("DONE SCN!", result, reloadRequired, complexWLIdnew, complexWLIdExtNew);

        return Response.ok(message).cacheControl(getNoCacheControl()).build();
    }

    private boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) {
        return iScnWorklogLogtimeStore.updateScnWorklog(_worklogId, _worklogType, _timeSpent, _comment);
    }

    private boolean deleteScnWorklog(Long worklogId) {
        return iScnWorklogLogtimeStore.deleteScnWorklogById(worklogId);
    }
}
