package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Objects;

public class CreateScnWorklogAction extends AbstractScnWorklogAction {
    private static final long serialVersionUID = 7460600544618806144L;

    private static final String SECURITY_BREACH = "securitybreach";

    private final FieldVisibilityManager fvManager;

    private IScnWorklog worklog;
    private Long newEstimateLong;
    private Long adjustmentAmountLong;

    public CreateScnWorklogAction(ProjectRoleManager projectRoleManager,
                                  GroupManager groupManager,
                                  IScnWorklogService scnWorklogService,
                                  ExtendedConstantsManager extendedConstantsManager) {
        super(ComponentAccessor.getComponent(CommentService.class), projectRoleManager,
            ComponentAccessor.getComponent(JiraDurationUtils.class), groupManager,
            new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()),
            scnWorklogService, new ScnProjectSettingsManager(projectRoleManager, extendedConstantsManager), extendedConstantsManager);
        this.fvManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
    }

    protected boolean isTimeTrackingFieldHidden(Issue issue) {
        return fvManager.isFieldHidden("timetracking", issue);
    }

    @Override
    public String doDefault() throws Exception {
        if (!isIssueValid()) {
            return ERROR;
        }

        if (!scnWorklogService.hasPermissionToCreate(getJiraServiceContext(), getIssueObject())) {
            return SECURITY_BREACH;
        }

        if (isTimeTrackingFieldHidden(getIssueObject())) {
            return SECURITY_BREACH;
        } else {
            WorklogType defaultWorklogType = projectSettignsManager.getDefaultWorklogType(
                Objects.requireNonNull(getIssueObject().getProjectObject()).getId()
            );
            setWorklogType(defaultWorklogType != null ? defaultWorklogType.getId() : "");
            setStartDate(getFormattedStartDate(new Date()));
            return super.doDefault();
        }
    }

    @Override
    protected void doValidation() {
        if (!isIssueValid()) {
            return;
        }
        CommentVisibility visibility = getCommentVisibility();
        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate)) {
            IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = scnWorklogService
                .validateCreateWithNewEstimate(getJiraServiceContext(), getIssueObject(), getTimeLogged(),
                    getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
                    getNewEstimate(), getWorklogType());
            if (worklogNewEstimateResult != null) {
                worklog = worklogNewEstimateResult.getWorklog();
                newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate)) {
            IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult = scnWorklogService
                .validateCreateWithManuallyAdjustedEstimate(getJiraServiceContext(), getIssueObject(),
                    getTimeLogged(), getParsedStartDate(), getComment(), visibility.getGroupLevel(),
                    visibility.getRoleLevel(), getAdjustmentAmount(), getWorklogType());
            if (worklogAdjustmentAmountResult != null) {
                worklog = worklogAdjustmentAmountResult.getWorklog();
                adjustmentAmountLong = worklogAdjustmentAmountResult.getAdjustmentAmount();
            }
        } else {
            worklog = scnWorklogService.validateCreate(getJiraServiceContext(), getIssueObject(), getTimeLogged(),
                getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
                getWorklogType());
        }

        if (projectSettignsManager.isWLTypeRequired(Objects.requireNonNull(getIssueObject().getProjectObject()).getId())
            && StringUtils.isBlank(getWorklogType()))
            getJiraServiceContext().getErrorCollection().addError("worklogType",
                getJiraServiceContext().getI18nBean().getText("logwork.worklogtype.error.null"));

        ApplicationUser reporter = null;
        if (!StringUtils.isBlank(getInputReporter())) {
            reporter = ComponentAccessor.getUserManager().getUserByKey(getInputReporter());
        }

        if (reporter == null) {
            getJiraServiceContext().getErrorCollection().addError("inputReporter",
                getJiraServiceContext().getI18nBean().getText("logwork.reporter.error.null"));
        } else if (worklog != null) {
            worklog = reassignWorklog(worklog, reporter);
        }
    }

    @Override
    protected String doExecute() {
        if (isTimeTrackingFieldHidden(getIssueObject()))
            return SECURITY_BREACH;
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(adjustEstimate))
            worklog = scnWorklogService.createAndAutoAdjustRemainingEstimate(getJiraServiceContext(), worklog, true,
                isWlAutoCopy());
        else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
            worklog = scnWorklogService.createWithNewRemainingEstimate(getJiraServiceContext(),
                new IScnWorklogService.WorklogNewEstimateResult(worklog, newEstimateLong), true, isWlAutoCopy());
        else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate))
            worklog = scnWorklogService.createWithManuallyAdjustedEstimate(getJiraServiceContext(),
                new IScnWorklogService.WorklogAdjustmentAmountResult(worklog, adjustmentAmountLong), true,
                isWlAutoCopy());
        else
            worklog = scnWorklogService.createAndRetainRemainingEstimate(getJiraServiceContext(), worklog, true,
                isWlAutoCopy());
        if (getHasErrorMessages())
            return ERROR;
        else if (isInlineDialogMode())
            return returnComplete();
        else
            return getRedirect("/browse/" + getIssue().getString("key"));
    }

    public IScnWorklog getWorklog() {
        return worklog;
    }

    public boolean isCreateWorklog() {
        return true;
    }
}
