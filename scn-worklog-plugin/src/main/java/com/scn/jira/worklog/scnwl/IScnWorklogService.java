package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 10.08.2010
 * Time: 15:09:03
 * To change this template use File | Settings | File Templates.
 */
public interface IScnWorklogService {
    public boolean isBlocked(JiraServiceContext jiraServiceContext, IScnWorklog wl);

    public IScnWorklog validateDelete(JiraServiceContext paramJiraServiceContext, Long paramLong);

    public WorklogNewEstimateResult validateDeleteWithNewEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString);

    public WorklogAdjustmentAmountResult validateDeleteWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString);

    public boolean deleteWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    public boolean deleteWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, WorklogAdjustmentAmountResult paramWorklogAdjustmentAmountResult, boolean paramBoolean, boolean isLinkedWL);

    public boolean deleteAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public boolean deleteAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog validateUpdate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String worklogTypeId);

    public WorklogNewEstimateResult validateUpdateWithNewEstimate(JiraServiceContext paramJiraServiceContext, Long paramLong, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    public IScnWorklog updateWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog updateAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog updateAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public boolean hasPermissionToUpdate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog);

    public boolean hasPermissionToDelete(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog);

    public boolean hasPermissionToView(JiraServiceContext jiraServiceContext, Issue issue);    

    public IScnWorklog validateCreate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String worklogTypeId);

    public WorklogNewEstimateResult validateCreateWithNewEstimate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    public WorklogAdjustmentAmountResult validateCreateWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, Issue paramIssue, String paramString1, Date paramDate, String paramString2, String paramString3, String paramString4, String paramString5, String worklogTypeId);

    public IScnWorklog createWithNewRemainingEstimate(JiraServiceContext paramJiraServiceContext, WorklogNewEstimateResult paramWorklogNewEstimateResult, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog createWithManuallyAdjustedEstimate(JiraServiceContext paramJiraServiceContext, WorklogAdjustmentAmountResult paramWorklogAdjustmentAmountResult, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog createAndRetainRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public IScnWorklog createAndAutoAdjustRemainingEstimate(JiraServiceContext paramJiraServiceContext, IScnWorklog paramWorklog, boolean paramBoolean, boolean isLinkedWL);

    public boolean hasPermissionToCreate(JiraServiceContext paramJiraServiceContext, Issue paramIssue);

    public IScnWorklog getById(JiraServiceContext paramJiraServiceContext, Long paramLong);

    public List<IScnWorklog> getByIssue(JiraServiceContext paramJiraServiceContext, Issue paramIssue);

    List<IScnWorklog> getByProjectBetweenDates(JiraServiceContext jiraServiceContext, Project project, Date startDate, Date endDate);

    public List<IScnWorklog> getByIssueVisibleToUser(JiraServiceContext paramJiraServiceContext, Issue paramIssue);

    public boolean isTimeTrackingEnabled();

    public boolean isIssueInEditableWorkflowState(Issue paramIssue);

    public List<IScnWorklog> getScnWorklogsByType(JiraServiceContext jiraServiceContext, String worklogTypeId);

    public List<IScnWorklog> getByProjectVisibleToUserBetweenDates(JiraServiceContext jiraServiceContext, Project project, Date startDate, Date endDate);

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
