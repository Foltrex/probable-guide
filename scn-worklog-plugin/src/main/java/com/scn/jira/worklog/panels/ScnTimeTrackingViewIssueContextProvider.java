package com.scn.jira.worklog.panels;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.viewissue.TimeTrackingViewIssueContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory.Style;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.atlassian.jira.issue.util.AggregateTimeTrackingBean.addAndPreserveNull;

public class ScnTimeTrackingViewIssueContextProvider extends TimeTrackingViewIssueContextProvider
{
    private static final String EPIC_ISSUE_TYPE_NAME = "Epic";

    private final JiraAuthenticationContext jaContext;
    private final PermissionManager permissionManager;
    private final IScnExtendedIssueStore eiStore;
    private final JiraDurationUtils utils;

    @Inject
    public ScnTimeTrackingViewIssueContextProvider(
        JiraAuthenticationContext authenticationContext,
        PermissionManager permissionManager,
        IScnExtendedIssueStore eiStore
    )
    {
        super(authenticationContext, ComponentAccessor.getComponent(AggregateTimeTrackingCalculatorFactory.class),
            ComponentAccessor.getComponent(TimeTrackingGraphBeanFactory.class));

        this.jaContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.eiStore = eiStore;
        this.utils = Assertions.notNull("utils", ComponentAccessor.getComponent(JiraDurationUtils.class));
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> params)
    {
        Map<String, Object> context = JiraVelocityUtils.getDefaultVelocityParams(params, jaContext);
        context.put("i18n", jaContext.getI18nHelper());

        Issue issue = (Issue)context.get("issue");
        IScnExtendedIssue extIssue = getExtendedIssue(issue);

        TimeTrackingGraphBean ttGraphBean = getGraphBean(
            extIssue.getOriginalEstimate(),
            extIssue.getEstimate(),
            extIssue.getTimeSpent());
        context.put("timeTrackingGraphBean", ttGraphBean);

        IssueType issueType = issue.getIssueType();
        if (issueType != null && EPIC_ISSUE_TYPE_NAME.equalsIgnoreCase(issueType.getName())) {
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
        }

        AggregateTimeTrackingBean aggBean = getAggregateTimeTrackingBean(extIssue);
        if (aggBean.getSubTaskCount() > 0)
        {
            TimeTrackingGraphBean aggGraphBean = getGraphBean(
                aggBean.getOriginalEstimate(),
                aggBean.getRemainingEstimate(),
                aggBean.getTimeSpent());
            context.put("aggregateTimeTrackingGraphBean", aggGraphBean);
            context.put("hasData", ttGraphBean.hasData() || aggGraphBean.hasData());
        }
        else
        {
            context.put("hasData", ttGraphBean.hasData());
        }

//        Delete later
        context.put("hasData", true);
//
        context.put("scn", "scn_");

        return context;
    }

    public List<Issue> getIssuesInEpic(Long epicId) {
        IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager();
        return issueLinkManager.getIssueLinks(epicId)
            .stream()
            .map(IssueLink::getDestinationObject)
            .collect(Collectors.toList());
    }

    @Nonnull
    private TimeTrackingGraphBean getGraphBean(Long originalEstimate, Long remainingEstimate, Long timeSpent)
    {
        Locale locale = jaContext.getI18nHelper().getLocale();
        Style style = TimeTrackingGraphBeanFactory.Style.SHORT;

        TimeTrackingGraphBean.Parameters params = new TimeTrackingGraphBean.Parameters(jaContext.getI18nHelper());
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

    private AggregateTimeTrackingBean getAggregateTimeTrackingBeanWithTasks(IScnExtendedIssue extIssue) {
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
            if (permissionManager.hasPermission(Permissions.BROWSE, extTask.getIssue(), jaContext.getLoggedInUser())) {
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
        for (Issue subTask : subTasks)
        {
            if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jaContext.getUser()))
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
            if (permissionManager.hasPermission(Permissions.BROWSE, extTask.getIssue(), jaContext.getLoggedInUser())) {
                bean.setTimeSpent(addAndPreserveNull(extTask.getTimeSpent(), bean.getTimeSpent()));
                bean.setRemainingEstimate(addAndPreserveNull(extTask.getEstimate(), bean.getRemainingEstimate()));
                bean.setOriginalEstimate(addAndPreserveNull(extTask.getOriginalEstimate(), bean.getOriginalEstimate()));

                for (Issue subTask : issueInEpic.getSubTaskObjects()) {
                    if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jaContext.getUser())) {
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

    private AggregateTimeTrackingBean getAggregateTimeTrackingBean(IScnExtendedIssue extIssue)
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
            if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jaContext.getUser()))
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

    private IScnExtendedIssue getExtendedIssue(Issue issue)
    {
        IScnExtendedIssue extIssue = eiStore.getByIssue(issue);

        if (extIssue == null)
        {
            extIssue = new ScnExtendedIssue(issue, null, null, null, null);
        }

        return extIssue;
    }
}
