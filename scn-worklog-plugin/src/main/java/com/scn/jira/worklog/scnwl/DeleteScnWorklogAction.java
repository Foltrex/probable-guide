package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;

public class DeleteScnWorklogAction extends AbstractScnWorklogAction {
    private static final long serialVersionUID = -7662358199449964631L;

    private IScnWorklog worklog;
    private Long newEstimateLong;
    private Long adjustmentAmountLong;

    public DeleteScnWorklogAction(CommentService commentService,
                                  ProjectRoleManager projectRoleManager,
                                  GroupManager groupManager,
                                  IScnWorklogService scnWorklogService) {
        super(commentService, projectRoleManager, ComponentAccessor.getComponent(JiraDurationUtils.class), groupManager,
            new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()),
            scnWorklogService, new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager()),
            new DefaultExtendedConstantsManager());
    }

    @Override
    public String doDefault() throws Exception {
        this.worklog = this.scnWorklogService.getById(getJiraServiceContext(), getWorklogId());
        if (this.worklog == null) {
            addErrorMessage(getJiraServiceContext().getI18nBean().getText("logwork.error.update.invalid.id",
                (getWorklogId() == null) ? null : getWorklogId().toString()));
            return "error";
        }
        if (!this.scnWorklogService.hasPermissionToDelete(getJiraServiceContext(), this.worklog)) {
            return "securitybreach";
        }
        setWorklogType(this.worklog.getWorklogTypeId());
        return super.doDefault();
    }

    @Override
    protected void doValidation() {
        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
            IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = this.scnWorklogService
                .validateDeleteWithNewEstimate(getJiraServiceContext(), getWorklogId(), getNewEstimate());

            if (worklogNewEstimateResult != null) {
                this.worklog = worklogNewEstimateResult.getWorklog();
                this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(this.adjustEstimate)) {
            IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult = this.scnWorklogService
                .validateDeleteWithManuallyAdjustedEstimate(getJiraServiceContext(), getWorklogId(),
                    getAdjustmentAmount());

            if (worklogAdjustmentAmountResult != null) {
                this.worklog = worklogAdjustmentAmountResult.getWorklog();
                this.adjustmentAmountLong = worklogAdjustmentAmountResult.getAdjustmentAmount();
            }
        } else {
            this.worklog = this.scnWorklogService.validateDelete(getJiraServiceContext(), getWorklogId());
        }
    }

    @Override
    protected String doExecute() {
        setWorklogType(this.worklog.getWorklogTypeId());
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteAndAutoAdjustRemainingEstimate(getJiraServiceContext(), this.worklog, true,
                isWlAutoCopy());
        } else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteWithNewRemainingEstimate(getJiraServiceContext(),
                new IScnWorklogService.WorklogNewEstimateResult(this.worklog, this.newEstimateLong), true,
                isWlAutoCopy());
        } else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteWithManuallyAdjustedEstimate(getJiraServiceContext(),
                new IScnWorklogService.WorklogAdjustmentAmountResult(this.worklog, this.adjustmentAmountLong), true,
                isWlAutoCopy());
        } else {
            this.scnWorklogService.deleteAndRetainRemainingEstimate(getJiraServiceContext(), this.worklog, true,
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

    public IScnWorklog getWorklog() {
        return this.worklog;
    }

    public boolean isWlAutoCopyDisabled() {
        return getWorklog() == null || getWorklog().getLinkedWorklog() == null;
    }

    @Override
    public boolean isWlAutoCopyChecked() {
        if (getJiraServiceContext().getErrorCollection().hasAnyErrors())
            return isWlAutoCopy();

        return this.worklog.getLinkedWorklog() != null;
    }
}
