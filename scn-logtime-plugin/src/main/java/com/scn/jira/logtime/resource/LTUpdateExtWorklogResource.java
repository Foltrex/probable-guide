package com.scn.jira.logtime.resource;

import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl2;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
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
import java.util.Objects;

@Named
@Path("/updateExtWorklog")
public class LTUpdateExtWorklogResource extends BaseResource {
    private static final Logger LOGGER = Logger.getLogger(LTUpdateExtWorklogResource.class);

    private IssueManager issueManager;
    private WorklogManager worklogManager;
    private ExtendedWorklogManager extendedWorklogManager;
    private IScnProjectSettingsManager projectSettignsManager;
    private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
    private WorklogService worklogService;

    @Inject
    public LTUpdateExtWorklogResource(@ComponentImport JiraAuthenticationContext authenticationContext,
                                      @ComponentImport IssueManager issueManager,
                                      @ComponentImport ProjectRoleManager projectRoleManager,
                                      @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
                                      @ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
                                      @ComponentImport ScnProjectSettingsManager projectSettignsManager,
                                      @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
                                      @ComponentImport DefaultScnWorklogService scnDefaultWorklogService,
                                      @ComponentImport WorklogService worklogService) {
        this.authenticationContext = authenticationContext;
        this.issueManager = issueManager;
        this.worklogManager = overridedWorklogManager;
        this.extendedWorklogManager = extendedWorklogManager;
        this.projectSettignsManager = projectSettignsManager;
        this.worklogService = worklogService;
        this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager, overridedWorklogManager,
            projectSettignsManager, scnUserBlockingManager, scnDefaultWorklogService);
    }

    @GET
    @AnonymousAllowed
    @Produces({"application/json", "application/xml"})
    public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("complexWLId") String complexWLId,
                                 @QueryParam("newValue") String newValue, @QueryParam("newWLType") String newWLType,
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

        @SuppressWarnings("unused")
        boolean result = false;
        boolean isValueEmplty = (newValue == null || newValue.equals("00:00") || newValue.equals("0") || newValue.equals("") || newValue.equals("0h"));
        // Check what to do with the worklog
        Long timeSpent;
        if (TextFormatUtil.matchesPattern1(newValue)) {
            timeSpent = TextFormatUtil.stringToTime(newValue);
        } else {
            if (TextFormatUtil.matchesPattern2(newValue)) {
                timeSpent = TextFormatUtil.string2ToTime(newValue);
            } else {
                if (TextFormatUtil.matchesPattern3(newValue)) {
                    timeSpent = TextFormatUtil.string3ToTime(newValue);
                } else {
                    LTMessages message = new LTMessages("DONE EXT!", false, false);
                    return Response.ok(message).build();
                }
            }
        }

        Issue issue = this.issueManager.getIssueObject(issueId);
        Project prj = issue.getProjectObject();
        ApplicationUser appUser = getLoggedInUser();
        if (prj != null) {
            boolean projectPermission = projectSettignsManager.hasPermissionToViewWL(appUser, prj);
            if (!projectPermission) {
                LOGGER.info("The user does not have permission to create Ext worklog");
                LTMessages message = new LTMessages("DONE EXT!", false, false);
                return Response.ok(message).build();
            }
        }
        Worklog worklog = worklogManager.getById(worklogId);
        Date day = DateUtils.stringToDate(date);
        boolean isBlocked = iScnWorklogLogtimeStore.isProjectWLWorklogBlocked(Objects.requireNonNull(prj).getId(), day);
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(appUser);
        if (isBlocked || (worklogId != 0 && isValueEmplty && !worklogService.hasPermissionToDelete(serviceContext, worklog))
            || (worklogId != 0 && !isValueEmplty && !worklogService.hasPermissionToUpdate(serviceContext, worklog))
            || (worklogId == 0) && !isValueEmplty && !worklogService.hasPermissionToCreate(serviceContext, issue, false)) {
            return Response.serverError()
                .entity(new ErrorEntity(
                    ErrorEntity.ErrorReason.APPLICATION_PERMISSION_DENIED,
                    serviceContext.getErrorCollection().getErrorMessages().stream().findFirst().orElse("Permission Denied (insufficient rights).")))
                .status(Response.Status.FORBIDDEN)
                .build();
        }

        Long wlIdExt = 0L;
        if (worklogId == 0) {
            if (day != null) {
                if (!isValueEmplty) {
                    if (timeSpent != 0) {
                        // Here we will create a worklog
                        wlIdExt = createExtWorklog(issueId, wlType, newValue, (comment != null) ? comment : "", userCreated, day);
                        if (!wlType.equals(worklogTypeId)) {
                            reloadRequired = true;
                        }
                    }
                }
            }
        } else if (worklog != null) {
            if (!isValueEmplty) {
                updateExtWorklog(issueId, worklogId, wlType, newValue, comment, day);
                if (!wlType.equals(worklogTypeId)) {
                    reloadRequired = true;
                }
                wlIdExt = worklogId;
            } else {
                deleteExtWorklog(worklogId);
            }
        }
        String complexWLIdnew = changeWlIdFromRequestParameter(complexWLId, wlIdExt);
        LTMessages message = new LTMessages("DONE EXT!", false, reloadRequired, complexWLIdnew);

        return Response.ok(message).cacheControl(getNoCacheControl()).build();
    }

    private Long createExtWorklog(Long issueId, String _worklogType, String _timeSpent, String _comment, String authorKey, Date date) {
        WorklogInputParameters parameters = new WorklogInputParametersImpl.Builder()
            .issue(issueManager.getIssueObject(issueId))
            .startDate(date)
            .timeSpent(_timeSpent)
            .comment(_comment)
            .build();
        WorklogResult result = worklogService.validateCreate(new JiraServiceContextImpl(getLoggedInUser()), parameters);
        if (result != null) {
            Worklog validatedWorklog = result.getWorklog();
            Long newEstimate = reduceEstimate(validatedWorklog.getIssue().getEstimate(), validatedWorklog.getTimeSpent());
            Worklog worklog = worklogManager.create(getLoggedInUser(),
                new WorklogImpl2(
                    validatedWorklog.getIssue(),
                    validatedWorklog.getId(),
                    authorKey,
                    validatedWorklog.getComment(),
                    validatedWorklog.getStartDate(),
                    validatedWorklog.getGroupLevel(),
                    validatedWorklog.getRoleLevelId(),
                    validatedWorklog.getTimeSpent(),
                    validatedWorklog.getRoleLevel()),
                newEstimate,
                false);
            extendedWorklogManager.createExtWorklogType(worklog, _worklogType);
            return worklog.getId();
        }
        return 0L;
    }

    private void updateExtWorklog(Long issueId, Long _worklogId, String _worklogType, String _timeSpent, String _comment, Date date) {
        WorklogInputParameters parameters = new WorklogInputParametersImpl.Builder()
            .issue(issueManager.getIssueObject(issueId))
            .worklogId(_worklogId)
            .startDate(date)
            .timeSpent(_timeSpent)
            .comment(_comment)
            .build();
        WorklogResult result = worklogService.validateUpdate(new JiraServiceContextImpl(getLoggedInUser()), parameters);
        if (result != null) {
            worklogService.updateAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(getLoggedInUser()), result, false);
            extendedWorklogManager.updateExtWorklogType(_worklogId, _worklogType);
        }
    }

    private void deleteExtWorklog(Long worklogId) {
        WorklogResult result = worklogService.validateDelete(new JiraServiceContextImpl(getLoggedInUser()), worklogId);
        if (result != null && worklogService.deleteAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(getLoggedInUser()), result, false))
            extendedWorklogManager.deleteExtWorklogType(worklogId);
    }

    @Nonnull
    private Long reduceEstimate(Long timeEstimate, @Nonnull Long amount) {
        long oldTimeEstimate = timeEstimate == null ? 0L : timeEstimate;
        long newTimeEstimate = oldTimeEstimate - amount;
        return Math.max(newTimeEstimate, 0L);
    }
}
