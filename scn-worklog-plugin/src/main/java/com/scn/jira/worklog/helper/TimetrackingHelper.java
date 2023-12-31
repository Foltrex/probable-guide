package com.scn.jira.worklog.helper;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.atlassian.jira.issue.util.AggregateTimeTrackingBean.addAndPreserveNull;

@Named
public class TimetrackingHelper {
    private static final String HAS_DATA = "hasData";
    private static final String EPIC_LINK_CUSTOM_FIELD_NAME = "Epic Link";

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraDurationUtils utils;
    private final IScnExtendedIssueStore issueStore;
    private final PermissionManager permissionManager;
    private final CustomFieldManager customFieldManager;

    @Inject
    public TimetrackingHelper(
        JiraAuthenticationContext jiraAuthenticationContext,
        PermissionManager permissionManager,
        IScnExtendedIssueStore issueStore,
        JiraDurationUtils jiraDurationUtils,
        CustomFieldManager customFieldManager
    ) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.issueStore = issueStore;
        this.utils = jiraDurationUtils;
        this.customFieldManager = customFieldManager;
    }


    private AggregateTimeTrackingBean getAggregateTimeTrackingBeanWithTasks(IScnExtendedIssue extIssue) {
        Assertions.notNull("extended issue", extIssue);
        Assertions.notNull("issue", extIssue.getIssue());

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
            extIssue.getOriginalEstimate(),
            extIssue.getEstimate(),
            extIssue.getTimeSpent(),
            0
        );

        for (Issue issueInEpic : getIssuesInEpic(extIssue.getIssue())) {
            final IScnExtendedIssue extTask = getExtendedIssue(issueInEpic);
            if (permissionManager.hasPermission(Permissions.BROWSE, extTask.getIssue(), jiraAuthenticationContext.getLoggedInUser())) {
                bean.setTimeSpent(addAndPreserveNull(extTask.getTimeSpent(), bean.getTimeSpent()));
                bean.setRemainingEstimate(addAndPreserveNull(extTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extTask.getOriginalEstimate(), bean.getOriginalEstimate()));
            }
        }
        return bean;
    }

    private AggregateTimeTrackingBean getAggregateTimeTrackingBeanWithTasksAndSubtask(IScnExtendedIssue extIssue) {
        Assertions.notNull("extended issue", extIssue);
        Assertions.notNull("issue", extIssue.getIssue());

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
            extIssue.getOriginalEstimate(), extIssue.getEstimate(), extIssue.getTimeSpent(), 0);

        final Collection<Issue> subTasks = extIssue.getIssue().getSubTaskObjects();

        int subTaskCount = 0;
        for (Issue subTask : subTasks) {
            if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jiraAuthenticationContext.getUser())) {
                final IScnExtendedIssue extSubTask = getExtendedIssue(subTask);

                bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
                bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
                bean.bumpGreatestSubTaskEstimate(extSubTask.getOriginalEstimate(), extSubTask.getEstimate(), extSubTask.getTimeSpent());

                subTaskCount++;
            }
        }
        bean.setSubTaskCount(subTaskCount);

        for (Issue issueInEpic : getIssuesInEpic(extIssue.getIssue())) {
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

    public IScnExtendedIssue getExtendedIssue(Issue issue) {
        IScnExtendedIssue extIssue = issueStore.getByIssue(issue);

        if (extIssue == null) {
            extIssue = new ScnExtendedIssue(issue, null, null, null, null);
        }

        return extIssue;
    }

    @Nonnull
    private TimeTrackingGraphBean getGraphBean(Long originalEstimate, Long remainingEstimate, Long timeSpent) {
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

    private List<Issue> getIssuesInEpic(Issue epicIssue) {
        IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager();
        CustomField epicLinkField = customFieldManager
            .getCustomFieldObjectsByName(EPIC_LINK_CUSTOM_FIELD_NAME)
            .stream()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);

        return issueLinkManager.getOutwardLinks(epicIssue.getId())
            .stream()
            .map(IssueLink::getDestinationObject)
            .filter(issue -> {
                Object epicLinkValue = issue.getCustomFieldValue(epicLinkField);
                String issueEpicKey = Objects.toString(epicLinkValue);
                return Objects.equals(epicIssue.getKey(), issueEpicKey);
            })
            .collect(Collectors.toList());
    }


    public void putEpicTimetrackingBeansIntoContext(Map<String, Object> context, IScnExtendedIssue extIssue) {
        context.put("isEpicIssue", true);

        AggregateTimeTrackingBean aggBeanWithTasks = getAggregateTimeTrackingBeanWithTasks(extIssue);
        TimeTrackingGraphBean aggGraphBeanWithTasks = getGraphBean(
            aggBeanWithTasks.getOriginalEstimate(),
            aggBeanWithTasks.getRemainingEstimate(),
            aggBeanWithTasks.getTimeSpent());
        context.put("aggregateTimeTrackingGraphBeanWithTasks", aggGraphBeanWithTasks);

        AggregateTimeTrackingBean aggBeanWithTaskAdnSubtasks = getAggregateTimeTrackingBeanWithTasksAndSubtask(extIssue);
        TimeTrackingGraphBean aggGraphBeanWithTasksAndSubtasks = getGraphBean(
            aggBeanWithTaskAdnSubtasks.getOriginalEstimate(),
            aggBeanWithTaskAdnSubtasks.getRemainingEstimate(),
            aggBeanWithTaskAdnSubtasks.getTimeSpent());
        context.put("aggregateTimeTrackingGraphBeanWithTasksAndSubtasks", aggGraphBeanWithTasksAndSubtasks);

        context.put(HAS_DATA, aggGraphBeanWithTasks.hasData() || aggGraphBeanWithTasksAndSubtasks.hasData());
    }
}
