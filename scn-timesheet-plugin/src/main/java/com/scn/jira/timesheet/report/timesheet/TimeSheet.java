package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
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
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.scn.jira.timesheet.report.pivot.Pivot;
import com.scn.jira.timesheet.util.TextUtil;
import com.scn.jira.timesheet.util.UserToKeyFunction;
import com.scn.jira.timesheet.util.WeekPortletHeader;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

@Named
@RequiredArgsConstructor
@Log4j
public class TimeSheet extends AbstractReport {
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final IGlobalSettingsManager scnGlobalPermissionManager;
    private final UserManager userManager;
    private final SearchProvider searchProvider;
    private final VisibilityValidator visibilityValidator;
    private final ProjectRoleManager projectRoleManager;
    private final GroupManager groupManager;
    private final SearchRequestManager searchRequestManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final DateTimeFormatter formatter = ComponentAccessor.getComponent(DateTimeFormatterFactory.class)
        .formatter().forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.ISO_8601_DATE);

    @Override
    public boolean showReport() {
        final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        return this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, user);
    }

    @Override
    public boolean isExcelViewSupported() {
        return true;
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
    public void validate(ProjectActionSupport action, Map params) {
        Date startDate = null;
        Date endDate = null;
        I18nBean i18nBean = new I18nBean(action.getLoggedInUser());
        try {
            endDate = Pivot.getEndDate(params, formatter);
        } catch (IllegalArgumentException e) {
            action.addError("endDate", "Format date error!", ErrorCollection.Reason.VALIDATION_FAILED);
        }
        try {
            startDate = Pivot.getStartDate(params, formatter, endDate);
        } catch (IllegalArgumentException e) {
            action.addError("startDate", "Format date error!", ErrorCollection.Reason.VALIDATION_FAILED);
        }
        if ((startDate == null) || (endDate == null) || (!endDate.before(startDate)))
            return;
        action.addError("endDate", i18nBean.getText("report.pivot.before.startdate"), ErrorCollection.Reason.VALIDATION_FAILED);
    }

    public TimeSheetDto getTimeSpents(ApplicationUser appUser, Date startDate, Date endDate, List<String> targetUserKeys,
                                      boolean excelView, String priority, List<String> targetGroups, List<Long> projectIds, Long filterId, Boolean showWeekends,
                                      Boolean showUsers, String groupByField, DateTimeFormatter formatter) throws SearchException {
        TimeSheetDto resultDto = new TimeSheetDto();

        if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, appUser)) {
            return resultDto;
        }

        final List<WeekPortletHeader> weekDays = resultDto.getWeekDays();
        final Map<Issue, List<IScnWorklog>> allWorkLogs = resultDto.getAllWorkLogs();
        final Map<ApplicationUser, Map<Issue, Map<IScnWorklog, Long>>> weekWorkLog = resultDto.getWeekWorkLog();
        final Map<Issue, Map<Date, Long>> weekWorkLogShort = resultDto.getWeekWorkLogShort();
        final Map<ApplicationUser, Map<Date, Long>> userWorkLogShort = resultDto.getUserWorkLogShort();
        final Map<Long, Long> weekTotalTimeSpents = resultDto.getWeekTotalTimeSpents();
        final Map<ApplicationUser, Map<Issue, Long>> userTotalTimeSpents = resultDto.getUserTotalTimeSpents();
        final Map<Project, Map<Date, Long>> projectTimeSpents = resultDto.getProjectTimeSpents();
        final Map<Project, Map<String, Map<Date, Long>>> projectGroupedByFieldTimeSpents = resultDto.getProjectGroupedByFieldTimeSpents();

        Set<Long> filteredIssues = new TreeSet<>();
        if (filterId != null) {
            log.info("Using filter: " + filterId);
            SearchRequest filter = this.searchRequestManager.getSearchRequestById(appUser, filterId);
            if (filter != null) {
                SearchQuery searchQuery = SearchQuery.create(filter.getQuery(), appUser);
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
        } else {
            conditions.add(new EntityExpr("author", IN, targetUserKeys));
        }

        List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);

        log.info("Query returned : " + worklogs.size() + " worklogs");

        for (GenericValue genericWorklog : worklogs) {
            Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("issue"));
            IScnWorklog worklog = WorklogUtil.convertToWorklog(projectRoleManager, genericWorklog, issue);

            boolean isValidVisibility = this.visibilityValidator.isValidVisibilityData(
                new JiraServiceContextImpl(appUser), "worklog", worklog.getIssue(),
                Visibilities.fromGroupAndStrRoleId(worklog.getGroupLevel(),
                    (worklog.getRoleLevelId() != null) ? worklog.getRoleLevelId().toString() : null));

            if (!isValidVisibility) {
                continue;
            }

            if ((filterId != null) && (!filteredIssues.contains(issue.getId()))) {
                continue;
            }

            Project project = issue.getProjectObject();

            if (StringUtils.isNotBlank(priority) && (!priority.equals(issue.getPriority().getId()))) {
                continue;
            }

            if (CollectionUtils.isNotEmpty(projectIds) && !projectIds.contains(project.getId())) {
                continue;
            }

            ApplicationUser workedUser = this.userManager.getUserByKey(genericWorklog.getString("author"));

            Date dateCreated = worklog.getStartDate();
            WeekPortletHeader weekDay = new WeekPortletHeader(dateCreated);
            if ((showWeekends != null) && (!showWeekends) && (weekDay.isNonBusinessDay())) {
                continue;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateCreated);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date dateOfTheDay = cal.getTime();
            Long dateCreatedLong = cal.getTimeInMillis();

            if (!this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, appUser)) {
                continue;
            }

            if (excelView) {
                List<IScnWorklog> issueWorklogs = allWorkLogs.computeIfAbsent(issue, k -> new ArrayList<>());
                issueWorklogs.add(worklog);
            } else {
                Map<Date, Long> weekTimeSpents = weekWorkLogShort.computeIfAbsent(issue, k -> new Hashtable<>());
                long spent = worklog.getTimeSpent();
                Long dateSpent = weekTimeSpents.get(dateOfTheDay);
                if (dateSpent != null) {
                    spent += dateSpent;
                }
                weekTimeSpents.put(dateOfTheDay, spent);
                updateUserWorkLog(worklog, workedUser, dateOfTheDay, userWorkLogShort);
                Map<Date, Long> projectWorkLog = projectTimeSpents.computeIfAbsent(project, k -> new Hashtable<>());
                spent = worklog.getTimeSpent();
                Long projectSpent = projectWorkLog.get(dateOfTheDay);
                if (projectSpent != null) {
                    spent += projectSpent;
                }
                projectWorkLog.put(dateOfTheDay, spent);
                calculateTimesForProjectGroupedByField(groupByField, worklog, issue, project, dateOfTheDay, formatter, projectGroupedByFieldTimeSpents);
                spent = worklog.getTimeSpent();
                dateSpent = weekTotalTimeSpents.get(dateCreatedLong);
                if (dateSpent != null) {
                    spent += dateSpent;
                }
                weekTotalTimeSpents.put(dateCreatedLong, spent);
                spent = worklog.getTimeSpent();
                if ((showUsers != null) && (showUsers)) {
                    Map<Issue, Map<IScnWorklog, Long>> userWorkLog = weekWorkLog
                        .get(workedUser);
                    if (userWorkLog == null) {
                        userWorkLog = new TreeMap<>(new IssueProjectComparator());
                        weekWorkLog.put(workedUser, userWorkLog);
                    }
                    Map<IScnWorklog, Long> issueWorkLog = userWorkLog.computeIfAbsent(issue, k -> new Hashtable<>());
                    issueWorkLog.put(worklog, spent);
                    spent = worklog.getTimeSpent();
                    Map<Issue, Long> issueTotalTimeSpents = userTotalTimeSpents.get(workedUser);
                    if (issueTotalTimeSpents == null) {
                        issueTotalTimeSpents = new TreeMap<Issue, Long>(new IssueKeyComparator());
                        userTotalTimeSpents.put(workedUser, issueTotalTimeSpents);
                    }
                    Long issueSpent = issueTotalTimeSpents.get(issue);
                    if (issueSpent != null) {
                        spent += issueSpent;
                    }
                    issueTotalTimeSpents.put(issue, spent);
                }
            }
        }
        // I18nBean i18nBean = new I18nBean(remoteUser);

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(startDate);
        while (endDate.after(calendarDate.getTime())) {
            WeekPortletHeader wph = new WeekPortletHeader();
            wph.setWeekDayDate(calendarDate.getTime());
            String businessDay = "";
            if ((calendarDate.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE))
                && (calendarDate.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH))
                && (calendarDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))) {
                businessDay = "toDay";
            } else if (wph.isNonBusinessDay()) {
                businessDay = "nonBusinessDay";
            }
            wph.setWeekDayCSS(businessDay);
            if ((showWeekends == null) || (showWeekends) || (!wph.isNonBusinessDay())) {
                weekDays.add(wph);
            }
            calendarDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        return resultDto;
    }

    private void updateUserWorkLog(IScnWorklog worklog, ApplicationUser workedUser, Date dateOfTheDay, Map<ApplicationUser, Map<Date, Long>> userWorkLogShort) {
        Map<Date, Long> dateToWorkMap = userWorkLogShort.computeIfAbsent(workedUser, k -> new HashMap<>());

        long spent = worklog.getTimeSpent();
        Long dateSpent = dateToWorkMap.get(dateOfTheDay);
        if (dateSpent != null) {
            spent += dateSpent;
        }
        dateToWorkMap.put(dateOfTheDay, spent);
    }

    private void calculateTimesForProjectGroupedByField(String groupByFieldID, IScnWorklog worklog, Issue issue,
                                                        Project project, Date dateOfTheDay, DateTimeFormatter formatter,
                                                        Map<Project, Map<String, Map<Date, Long>>> projectGroupedByFieldTimeSpents) {
        if (groupByFieldID == null) {
            return;
        }
        String fieldValue = TextUtil.getFieldValue(groupByFieldID, issue, formatter);

        Map<String, Map<Date, Long>> projectToFieldWorkLog = projectGroupedByFieldTimeSpents.computeIfAbsent(project, k -> new Hashtable<>());

        Map<Date, Long> fieldToTimeWorkLog = projectToFieldWorkLog.computeIfAbsent(fieldValue, k -> new Hashtable<>());

        long spent = worklog.getTimeSpent();
        Long projectGroupedSpent = fieldToTimeWorkLog.get(dateOfTheDay);

        if (projectGroupedSpent != null) {
            spent += projectGroupedSpent;
        }

        fieldToTimeWorkLog.put(dateOfTheDay, spent);
    }

    public String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView) throws Exception {
        ApplicationUser remoteUser = action.getLoggedInUser();
        I18nBean i18nBean = new I18nBean(remoteUser);

        Date endDate = Pivot.getEndDate(params, formatter);
        Date startDate = Pivot.getStartDate(params, formatter, endDate);

        List<String> targetUserKeys = Stream.of(ParameterUtils.getStringParam(params, "targetUser").split(","))
            .filter(StringUtils::isNotBlank)
            .map(userManager::getUserByName)
            .filter(Objects::nonNull)
            .map(ApplicationUser::getKey)
            .collect(Collectors.toList());
        String priority = ParameterUtils.getStringParam(params, "priority");
        List<String> targetGroups = null;
        if (StringUtils.isNotBlank(ParameterUtils.getStringParam(params, "targetGroup"))) {
            targetGroups = ParameterUtils.getListParam(params, "targetGroup") == null
                ? Collections.singletonList(ParameterUtils.getStringParam(params, "targetGroup"))
                : ParameterUtils.getListParam(params, "targetGroup");
        }
        List<String> projectStringList = ParameterUtils.getListParam(params, "project") == null
            ? Collections.singletonList(ParameterUtils.getStringParam(params, "project"))
            : ParameterUtils.getListParam(params, "project");
        List<Long> projectIds = projectStringList.stream().filter(StringUtils::isNotBlank).map(Long::parseLong).collect(Collectors.toList());

        Long filterId = ParameterUtils.getLongParam(params, "filterid");

        Boolean showWeekends;
        if ("true".equalsIgnoreCase(ParameterUtils.getStringParam(params, "weekends")))
            showWeekends = Boolean.TRUE;
        else {
            showWeekends = Boolean.FALSE;
        }
        Boolean showUsers;
        if ("true".equalsIgnoreCase(ParameterUtils.getStringParam(params, "showUsers")))
            showUsers = Boolean.TRUE;
        else {
            showUsers = Boolean.FALSE;
        }
        if (targetUserKeys.isEmpty()) {
            targetUserKeys.add(remoteUser.getKey());
        }

        String groupByField = ParameterUtils.getStringParam(params, "groupByField");
        if (groupByField != null) {
            if (groupByField.trim().length() == 0) {
                groupByField = null;
            } else if (ComponentAccessor.getFieldManager().getField(groupByField) == null) {
                log.error("GroupByField ' " + groupByField + "' does not exist");
                groupByField = null;
            }
        }

        TimeSheetDto timeSheetDto = getTimeSpents(remoteUser, startDate, endDate, targetUserKeys, excelView, priority, targetGroups,
            projectIds, filterId, showWeekends, showUsers, groupByField, formatter);

        Map<String, Object> velocityParams = new HashMap<>();
        velocityParams.put("startDate", startDate);
        velocityParams.put("endDate", endDate);
        velocityParams.put("weekDays", timeSheetDto.getWeekDays());
        velocityParams.put("showUsers", showUsers);

        if (excelView) {
            velocityParams.put("allWorkLogs", timeSheetDto.getAllWorkLogs());
        } else {
            if (showUsers)
                velocityParams.put("weekWorkLog", timeSheetDto.getWeekWorkLog());
            else {
                velocityParams.put("weekWorkLog", timeSheetDto.getWeekWorkLogShort());
            }
            velocityParams.put("weekTotalTimeSpents", timeSheetDto.getWeekTotalTimeSpents());
            velocityParams.put("userTotalTimeSpents", timeSheetDto.getUserTotalTimeSpents());
            velocityParams.put("projectTimeSpents", timeSheetDto.getProjectTimeSpents());
            velocityParams.put("projectGroupedTimeSpents", timeSheetDto.getProjectGroupedByFieldTimeSpents());
        }
        velocityParams.put("groupByField", groupByField);
        velocityParams.put("formatter", formatter);
        velocityParams.put("fieldVisibility", this.fieldVisibilityManager);
        velocityParams.put("textUtil", new TextUtil(i18nBean));

        return this.descriptor.getHtml((excelView) ? "excel" : "view", velocityParams);
    }
}
