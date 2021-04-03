package com.scn.jira.timesheet.report.pivot;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.jql.query.IssueIdCollector;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.scn.jira.timesheet.util.MyFullNameComparator;
import com.scn.jira.timesheet.util.MyUser;
import com.scn.jira.timesheet.util.TextUtil;
import com.scn.jira.timesheet.util.UserToKeyFunction;
import com.scn.jira.timesheet.util.WorklogUtil;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

@Named
@RequiredArgsConstructor
@Log4j
public class Pivot extends AbstractReport {
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final SearchProvider searchProvider;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final SearchRequestManager searchRequestManager;
    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;
    private final IGlobalSettingsManager scnGlobalPermissionManager;
    private final DateTimeFormatter formatter = ComponentAccessor.getComponent(DateTimeFormatterFactory.class)
        .formatter().forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.ISO_8601_DATE);

    private Date startDate;
    private Date endDate;

    private final Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<>();
    public Map<Issue, Map<MyUser, Long>> workedIssues = new TreeMap<Issue, Map<MyUser, Long>>(new IssueKeyComparator());
    public Map<MyUser, Long> workedUsers = new TreeMap<>(new MyFullNameComparator());
    public SearchRequest filter = null;

    @Override
    public boolean showReport() {
        final ApplicationUser user = this.authenticationContext.getLoggedInUser();
        return this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, user);
    }

    @Override
    public void validate(ProjectActionSupport action, Map params) {
        I18nBean i18nBean = new I18nBean(action.getLoggedInUser());
        try {
            endDate = Pivot.getEndDate(params, formatter);
        } catch (IllegalArgumentException e) {
            action.addError("endDate", "Format date error!", Reason.VALIDATION_FAILED);
        }
        try {
            startDate = Pivot.getStartDate(params, formatter, endDate);
        } catch (IllegalArgumentException e) {
            action.addError("startDate", "Format date error!", Reason.VALIDATION_FAILED);
        }
        if ((startDate == null) || (endDate == null) || (!endDate.before(startDate)))
            return;
        action.addError("endDate", i18nBean.getText("report.pivot.before.startdate"), Reason.VALIDATION_FAILED);
    }

    @Override
    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        return generateReport(action, params, false);
    }

    @Override
    public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception {
        StringBuilder contentDispositionValue = new StringBuilder(50);
        contentDispositionValue.append("attachment;filename=\"").append(this.getDescriptor().getName()).append(".xls\";");
        HttpServletResponse response = ActionContext.getResponse();
        if (response != null) {
            response.addHeader("Content-Disposition", contentDispositionValue.toString());
        }

        return "<meta charset=\"utf-8\"/>\n" + generateReport(action, params, true);
    }

    @Override
    public boolean isExcelViewSupported() {
        return true;
    }

    public void getTimeSpents(ApplicationUser remoteUser, Date startDate, Date endDate, List<Long> projectIds, Long filterId,
                              List<String> targetGroups, boolean excelView) throws SearchException {
        if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, remoteUser)) {
            return;
        }

        Set<Long> filteredIssues = new TreeSet<>();
        if (filterId != null) {
            log.info("Using filter: " + filterId);
            SearchRequest filter = this.searchRequestManager.getSearchRequestById(remoteUser, filterId);
            if (filter != null) {
                SearchQuery searchQuery = SearchQuery.create(filter.getQuery(), remoteUser);
                IssueIdCollector issueIdCollector = new IssueIdCollector();
                this.searchProvider.search(searchQuery, issueIdCollector);
                issueIdCollector.getIssueIds().forEach((id) -> {
                    filteredIssues.add(Long.parseLong(id));
                });
            }
        }

        List<EntityCondition> conditions = new ArrayList<>();
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
        conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
        if (CollectionUtils.isNotEmpty(targetGroups)) {
            Collection<String> userKeys = UserToKeyFunction.transform(targetGroups
                .stream()
                .flatMap(group -> this.groupManager.getUsersInGroup(group).stream())
                .distinct()
                .collect(Collectors.toList()));
            conditions.add(new EntityExpr("author", IN, CollectionUtils.isNotEmpty(userKeys) ? userKeys : Collections.singletonList("")));
            log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "', by group '"
                + targetGroups + "'");
        } else {
            log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "'");
        }

        List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);

        log.info("Query returned : " + worklogs.size() + " worklogs");
        for (GenericValue genericWorklog : worklogs) {
            Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("issue"));
            final IScnWorklog worklog = WorklogUtil.convertToWorklog(this.projectRoleManager, genericWorklog, issue);

            if ((issue != null) && (((CollectionUtils.isEmpty(projectIds)) || (projectIds.contains(Objects.requireNonNull(issue.getProjectObject()).getId()))))
                && (((filterId == null) || (filteredIssues.contains(issue.getId()))))
                && (this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, remoteUser))) {
                if (excelView) {
                    List<IScnWorklog> issueWorklogs = this.allWorkLogs.computeIfAbsent(issue, k -> new ArrayList<>());
                    issueWorklogs.add(worklog);
                } else {
                    Map<MyUser, Long> issueWorkLog = this.workedIssues.computeIfAbsent(issue, k -> new Hashtable<>());
                    MyUser user;
                    ApplicationUser appUser = ComponentAccessor.getUserManager().getUserByKey(worklog.getAuthorKey());
                    if (worklog.getAuthorKey() != null && appUser != null) {
                        user = new MyUser(appUser.getKey(), appUser.getDisplayName());
                    } else {
                        user = new MyUser("anonymous", "anonymous");
                    }
                    long timespent = worklog.getTimeSpent();
                    Long worked = issueWorkLog.get(user);
                    if (worked != null) {
                        timespent += worked;
                    }

                    worked = timespent;
                    issueWorkLog.put(user, worked);

                    timespent = worklog.getTimeSpent();
                    worked = this.workedUsers.get(user);
                    if (worked != null) {
                        timespent += worked;
                    }
                    worked = timespent;
                    this.workedUsers.put(user, worked);
                }
            }
        }
    }

    private String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView) throws Exception {
        ApplicationUser remoteUser = action.getLoggedInUser();
        I18nBean i18nBean = new I18nBean(remoteUser);
        List<String> projectStringList = ParameterUtils.getListParam(params, "projectid") == null
            ? Collections.singletonList(ParameterUtils.getStringParam(params, "projectid"))
            : ParameterUtils.getListParam(params, "projectid");
        List<Long> projectIds = projectStringList.stream().filter(StringUtils::isNotBlank).map(Long::parseLong).collect(Collectors.toList());
        Long filterId = ParameterUtils.getLongParam(params, "filterid");
        List<String> targetGroups = null;
        if (StringUtils.isNotBlank(ParameterUtils.getStringParam(params, "targetGroup"))) {
            targetGroups = ParameterUtils.getListParam(params, "targetGroup") == null
                ? Collections.singletonList(ParameterUtils.getStringParam(params, "targetGroup"))
                : ParameterUtils.getListParam(params, "targetGroup");
        }
        if (excelView) {
            validate(action, params);
        }
        getTimeSpents(remoteUser, startDate, endDate, projectIds, filterId, targetGroups, excelView);

        params.put("startDate", formatter.format(startDate));
        params.put("endDate", formatter.format(endDate));
        params.put("formatter", formatter);

        params.put("fieldVisibility", this.fieldVisibilityManager);
        params.put("textUtil", new TextUtil(i18nBean));

        if (excelView) {
            params.put("allWorkLogs", this.allWorkLogs);
        } else {
            params.put("workedIssues", this.workedIssues);
            params.put("workedUsers", this.workedUsers);
        }

        return this.descriptor.getHtml((excelView) ? "excel" : "view", params);
    }

    public static Date getStartDate(Map params, DateTimeFormatter formatter, Date endDate) throws IllegalArgumentException {
        String startDateString = ParameterUtils.getStringParam(params, "startDate");
        Date startDate;
        if (startDateString.isEmpty()) {
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(endDate);
            calendarDate.add(Calendar.WEEK_OF_YEAR, -1);
            startDate = calendarDate.getTime();
        } else
            startDate = formatter.parse(startDateString);

        return startDate;
    }

    public static Date getEndDate(Map params, DateTimeFormatter formatter) throws IllegalArgumentException {
        String endDateString = ParameterUtils.getStringParam(params, "endDate");
        Calendar calendarDate = Calendar.getInstance();
        if (endDateString.isEmpty()) {
            calendarDate.set(Calendar.HOUR_OF_DAY, 0);
            calendarDate.set(Calendar.MINUTE, 0);
            calendarDate.set(Calendar.SECOND, 0);
            calendarDate.set(Calendar.MILLISECOND, 0);
        } else {
            calendarDate.setTime(formatter.parse(endDateString));
            calendarDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendarDate.getTime();
    }
}
