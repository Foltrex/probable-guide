package com.scn.jira.worklog.helper;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.atlassian.jira.issue.util.AggregateTimeTrackingBean.addAndPreserveNull;

@Named
public class TimetrackingHelper {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraDurationUtils utils;
    private final IScnExtendedIssueStore issueStore;
    private final PermissionManager permissionManager;

    @Inject
    public TimetrackingHelper(
        JiraAuthenticationContext jiraAuthenticationContext,
        PermissionManager permissionManager,
        IScnExtendedIssueStore issueStore
    ) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.issueStore = issueStore;
        this.utils = Assertions.notNull("utils", ComponentAccessor.getComponent(JiraDurationUtils.class));
    }


    public AggregateTimeTrackingBean getAggregateTimeTrackingBeanWithTasks(IScnExtendedIssue extIssue) {
        Assertions.notNull("extended issue", extIssue);
        Assertions.notNull("issue", extIssue.getIssue());

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
            extIssue.getOriginalEstimate(),
            extIssue.getEstimate(),
            extIssue.getTimeSpent(),
            0
        );

        for (Issue issueInEpic : getIssuesInEpic(extIssue.getIssue().getId())) {
            final IScnExtendedIssue extTask = getExtendedIssue(issueInEpic);
            if (permissionManager.hasPermission(Permissions.BROWSE, extTask.getIssue(), jiraAuthenticationContext.getLoggedInUser())) {
                bean.setTimeSpent(addAndPreserveNull(extTask.getTimeSpent(), bean.getTimeSpent()));
                bean.setRemainingEstimate(addAndPreserveNull(extTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extTask.getOriginalEstimate(), bean.getOriginalEstimate()));
            }
        }
        return bean;
    }

    public AggregateTimeTrackingBean getAggregateTimeTrackingBeanWithTasksAndSubtask(IScnExtendedIssue extIssue) {
        Assertions.notNull("extended issue", extIssue);
        Assertions.notNull("issue", extIssue.getIssue());

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
            extIssue.getOriginalEstimate(), extIssue.getEstimate(), extIssue.getTimeSpent(), 0);

        final Collection<Issue> subTasks = extIssue.getIssue().getSubTaskObjects();

        int subTaskCount = 0;
        for (Issue subTask : subTasks)
        {
            if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jiraAuthenticationContext.getUser()))
            {
                final IScnExtendedIssue extSubTask = getExtendedIssue(subTask);

                bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
                bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
                bean.bumpGreatestSubTaskEstimate(extSubTask.getOriginalEstimate(), extSubTask.getEstimate(), extSubTask.getTimeSpent());

                subTaskCount++;
            }
        }
        bean.setSubTaskCount(subTaskCount);

        for (Issue issueInEpic : getIssuesInEpic(extIssue.getIssue().getId())) {
            final IScnExtendedIssue extTask = getExtendedIssue(issueInEpic);
            if (permissionManager.hasPermission(Permissions.BROWSE, extTask.getIssue(), jiraAuthenticationContext.getLoggedInUser())) {
                bean.setTimeSpent(addAndPreserveNull(extTask.getTimeSpent(), bean.getTimeSpent()));
                bean.setRemainingEstimate(addAndPreserveNull(extTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extTask.getOriginalEstimate(), bean.getOriginalEstimate()));

                for (Issue subTask : issueInEpic.getSubTaskObjects()) {
                    if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jiraAuthenticationContext.getUser())) {
                        final IScnExtendedIssue extSubTask = getExtendedIssue(subTask);
                        bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
                        bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
                        bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
                    }
                }
            }
        }

        return bean;
    }

    public IScnExtendedIssue getExtendedIssue(Issue issue)
    {
        IScnExtendedIssue extIssue = issueStore.getByIssue(issue);

        if (extIssue == null)
        {
            extIssue = new ScnExtendedIssue(issue, null, null, null, null);
        }

        return extIssue;
    }

    @Nonnull
    public TimeTrackingGraphBean getGraphBean(Long originalEstimate, Long remainingEstimate, Long timeSpent)
    {
        Locale locale = jiraAuthenticationContext.getI18nHelper().getLocale();
        TimeTrackingGraphBeanFactory.Style style = TimeTrackingGraphBeanFactory.Style.SHORT;

        TimeTrackingGraphBean.Parameters params = new TimeTrackingGraphBean.Parameters(jiraAuthenticationContext.getI18nHelper());
        params.setOriginalEstimate(originalEstimate);
        params.setRemainingEstimate(remainingEstimate);
        params.setTimeSpent(timeSpent);
        params.setTimeSpentStr(style.getDuration(timeSpent, locale, utils));
        params.setOriginalEstimateStr(style.getDuration(originalEstimate, locale, utils));
        params.setRemainingEstimateStr(style.getDuration(remainingEstimate, locale, utils));
        params.setTimeSpentTooltip(style.getTooltip(timeSpent, locale, utils));
        params.setOriginalEstimateTooltip(style.getTooltip(originalEstimate, locale, utils));
        params.setRemainingEstimateTooltip(style.getTooltip(remainingEstimate, locale, utils));
        return new TimeTrackingGraphBean(params);
    }

    public List<Issue> getIssuesInEpic(Long epicId) {
        IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager();
        return issueLinkManager.getIssueLinks(epicId)
            .stream()
            .map(IssueLink::getDestinationObject)
            .collect(Collectors.toList());
    }


    public AggregateTimeTrackingBean getAggregateTimeTrackingBean(IScnExtendedIssue extIssue)
    {
        Assertions.notNull("extended issue", extIssue);
        Assertions.notNull("issue", extIssue.getIssue());

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
            extIssue.getOriginalEstimate(), extIssue.getEstimate(), extIssue.getTimeSpent(), 0);
        if (extIssue.getIssue().isSubTask())
            return bean;

        final Collection<Issue> subTasks = extIssue.getIssue().getSubTaskObjects();
        if (subTasks == null || subTasks.isEmpty())
            return bean;

        int subTaskCount = 0;
        for (Issue subTask : subTasks)
        {
            if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jiraAuthenticationContext.getUser()))
            {
                final IScnExtendedIssue extSubTask = getExtendedIssue(subTask);

                bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
                bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
                bean.bumpGreatestSubTaskEstimate(extSubTask.getOriginalEstimate(), extSubTask.getEstimate(), extSubTask.getTimeSpent());

                subTaskCount++;
            }
        }
        bean.setSubTaskCount(subTaskCount);

        return bean;
    }

}
