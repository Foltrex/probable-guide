package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;

import java.util.Date;
import java.util.List;

public interface IScnWorklogService {
    boolean isBlocked(JiraServiceContext jiraServiceContext, IScnWorklog wl);

    IScnWorklog validateDelete(JiraServiceContext paramJiraServiceContext, Long paramLong);

    WorklogNewEstimateResult validateDeleteWithNewEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString);

    WorklogAdjustmentAmountResult validateDeleteWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString);

    boolean deleteWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    boolean deleteWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, WorklogAdjustmentAmountResult paramWorklogAdjustmentAmountResult, boolean paramBoolean, boolean isLinkedWL);

    boolean deleteAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    boolean deleteAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog validateUpdate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String worklogTypeId);

    WorklogNewEstimateResult validateUpdateWithNewEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    IScnWorklog updateWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog updateAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog updateAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    boolean hasPermissionToUpdate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog);

    boolean hasPermissionToDelete(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog);

    boolean hasPermissionToView(JiraServiceContext jiraServiceContext, Issue issue);

    IScnWorklog validateCreate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String worklogTypeId);

    WorklogNewEstimateResult validateCreateWithNewEstimate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    WorklogAdjustmentAmountResult validateCreateWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    IScnWorklog createWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog createWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, WorklogAdjustmentAmountResult paramWorklogAdjustmentAmountResult, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog createAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    IScnWorklog createAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    boolean hasPermissionToCreate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String authorKey);

    IScnWorklog getById(JiraServiceContext paramJiraServiceContext, Long paramLong);

    List<IScnWorklog> getByIssue(JiraServiceContext paramJiraServiceContext, Issue paramIssue);

    List<IScnWorklog> getByProjectBetweenDates(JiraServiceContext jiraServiceContext, Project project, Date startDate, Date endDate);

    List<IScnWorklog> getByIssueVisibleToUser(JiraServiceContext paramJiraServiceContext, Issue paramIssue);

    boolean isTimeTrackingEnabled();

    boolean isIssueInEditableWorkflowState(Issue paramIssue);

    List<IScnWorklog> getScnWorklogsByType(JiraServiceContext jiraServiceContext, String worklogTypeId);

    List<IScnWorklog> getByProjectVisibleToUserBetweenDates(JiraServiceContext jiraServiceContext, Project project, Date startDate, Date endDate);

    public static class WorklogAdjustmentAmountResult {
        private IScnWorklog worklog;
        private Long adjustmentAmount;

        public WorklogAdjustmentAmountResult(IScnWorklog worklog, Long adjustmentAmount) {
            this.worklog = worklog;
            this.adjustmentAmount = adjustmentAmount;
        }

        public IScnWorklog getWorklog() {
            return this.worklog;
        }

        public Long getAdjustmentAmount() {
            return this.adjustmentAmount;
        }
    }

    public static class WorklogNewEstimateResult {
        private IScnWorklog worklog;
        private Long newEstimate;

        public WorklogNewEstimateResult(IScnWorklog worklog, Long newEstimate) {
            this.worklog = worklog;
            this.newEstimate = newEstimate;
        }

        public IScnWorklog getWorklog() {
            return this.worklog;
        }

        public Long getNewEstimate() {
            return this.newEstimate;
        }
    }
}
