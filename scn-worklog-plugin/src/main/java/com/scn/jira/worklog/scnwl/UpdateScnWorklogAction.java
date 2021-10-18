package com.scn.jira.worklog.scnwl;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class UpdateScnWorklogAction extends AbstractScnWorklogAction {
    private static final long serialVersionUID = -5362590359328831184L;

    private Long newEstimateLong;
    private IScnWorklog worklog;

    public UpdateScnWorklogAction(ProjectRoleManager projectRoleManager,
                                  GroupManager groupManager,
                                  IScnWorklogService scnWorklogService,
                                  ExtendedConstantsManager extendedConstantsManager) {
        super(ComponentAccessor.getComponent(CommentService.class), projectRoleManager,
            ComponentAccessor.getComponent(JiraDurationUtils.class), groupManager,
            new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()),
            scnWorklogService, new ScnProjectSettingsManager(projectRoleManager, extendedConstantsManager), extendedConstantsManager);
    }

    @Override
    public String doDefault() throws Exception {
        this.worklog = this.scnWorklogService.getById(getJiraServiceContext(), getWorklogId());
        if (this.worklog == null) {
            addErrorMessage(getJiraServiceContext().getI18nBean().getText("logwork.error.update.invalid.id",
                (getWorklogId() == null) ? null : getWorklogId().toString()));
            return "error";
        }
        if (!this.scnWorklogService.hasPermissionToUpdate(getJiraServiceContext(), this.worklog)) {
            return "securitybreach";
        }

        setTimeLogged(DateUtils.getDurationString(this.worklog.getTimeSpent(), getHoursPerDay().intValue(),
            getDaysPerWeek().intValue()));
        setStartDate(getFormattedStartDate(this.worklog.getStartDate()));
        setComment(this.worklog.getComment());
        setWorklogType(this.worklog.getWorklogTypeId());
        setCommentLevel(CommentVisibility.getCommentLevelFromLevels(this.worklog.getGroupLevel(),
            this.worklog.getRoleLevelId()));

        return super.doDefault();
    }

    @Override
    protected void doValidation() {
        CommentVisibility visibility = getCommentVisibility();

        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
            IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = this.scnWorklogService
                .validateUpdateWithNewEstimate(getJiraServiceContext(), getWorklogId(), getTimeLogged(),
                    getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
                    getNewEstimate(), getWorklogType());

            if (worklogNewEstimateResult != null) {
                this.worklog = worklogNewEstimateResult.getWorklog();
                this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else {
            this.worklog = this.scnWorklogService.validateUpdate(getJiraServiceContext(), getWorklogId(),
                getTimeLogged(), getParsedStartDate(), getComment(), visibility.getGroupLevel(),
                visibility.getRoleLevel(), getWorklogType());
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
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.updateAndAutoAdjustRemainingEstimate(getJiraServiceContext(), this.worklog, true,
                isWlAutoCopy());
        } else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.updateWithNewRemainingEstimate(getJiraServiceContext(),
                new IScnWorklogService.WorklogNewEstimateResult(this.worklog, this.newEstimateLong), true,
                isWlAutoCopy());
        } else {
            this.scnWorklogService.updateAndRetainRemainingEstimate(getJiraServiceContext(), this.worklog, true,
                isWlAutoCopy());
        }

        if (getHasErrorMessages()) {
            return "error";
        }

        if (isInlineDialogMode()) {
            return returnComplete();
        }

        return getRedirect("/browse/" + getIssue().getString("key"));
    }

    @Override
    public boolean isWlAutoCopyChecked() {
        if (getJiraServiceContext().getErrorCollection().hasAnyErrors())
            return isWlAutoCopy();

        return this.worklog.getLinkedWorklog() != null;
    }

    public IScnWorklog getWorklog() {
        return this.worklog;
    }
}
