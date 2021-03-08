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
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.issue.search.DocumentWithId;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.scn.jira.timesheet.report.pivot.Pivot;
import com.scn.jira.timesheet.util.TextUtil;
import com.scn.jira.timesheet.util.UserToNameFunction;
import com.scn.jira.timesheet.util.WeekPortletHeader;
import com.scn.jira.timesheet.util.WorklogUtil;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.inject.Named;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
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
    private final DateTimeFormatter formatter = ComponentAccessor.getComponent(DateTimeFormatterFactory.class).formatter().forLoggedInUser()
        .withSystemZone().withStyle(DateTimeStyle.DATE_PICKER);

    private final List<WeekPortletHeader> weekDays = new ArrayList<>();
    private final Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<>();
    private final Map<ApplicationUser, Map<Issue, Map<IScnWorklog, Long>>> weekWorkLog = new TreeMap<>(new UserComparator());
    private final Map<Issue, Map<Date, Long>> weekWorkLogShort = new TreeMap<Issue, Map<Date, Long>>(new IssueKeyComparator());
    private final Map<ApplicationUser, Map<Date, Long>> userWorkLogShort = new TreeMap<>(new UserComparator());
    private final Map<Long, Long> weekTotalTimeSpents = new Hashtable<>();
    private final Map<ApplicationUser, Map<Issue, Long>> userTotalTimeSpents = new Hashtable<>();
    private final Map<Project, Map<Date, Long>> projectTimeSpents = new Hashtable<>();
    private final Map<Project, Map<String, Map<Date, Long>>> projectGroupedByFieldTimeSpents = new Hashtable<>();

    @Override
    public boolean showReport() {
        final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        return this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, user);
    }

    public Map<Long, Long> getWeekTotalTimeSpents() {
        return this.weekTotalTimeSpents;
    }

    public Map<ApplicationUser, Map<Date, Long>> getUserWorkLog() {
        return this.userWorkLogShort;
    }

    public Map<Issue, Map<Date, Long>> getWeekWorkLogShort() {
        return this.weekWorkLogShort;
    }

    public List<WeekPortletHeader> getWeekDays() {
        return this.weekDays;
    }

    @SuppressWarnings("unchecked")
    public void getTimeSpents(ApplicationUser appUser, Date startDate, Date endDate, String targetUserName,
                              boolean excelView, String priority, String targetGroup, Long projectId, Long filterId, Boolean showWeekends,
                              Boolean showUsers, String groupByField, DateTimeFormatter formatter)
        throws SearchException, GenericEntityException {

        if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, appUser)) {
            return;
        }

        Set<Long> filteredIssues = new TreeSet<>();
        if (filterId != null) {
            log.info("Using filter: " + filterId);
            SearchRequest filter = this.searchRequestManager.getSearchRequestById(appUser, filterId);
            if (filter != null) {

                SearchQuery searchQuery = SearchQuery.create(filter.getQuery(), appUser);
                SearchResults<DocumentWithId> issues = this.searchProvider.search(searchQuery,
                    PagerFilter.getUnlimitedFilter());
                for (Object result : issues.getResults()) {
                    if (result instanceof Issue) {
                        filteredIssues.add(((Issue) result).getId());
                    }
                }
            }
        }

        List<EntityCondition> conditions = new ArrayList<EntityCondition>();
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
        conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
        if (StringUtils.isNotEmpty(targetGroup)) {
            Collection<String> usersNames = UserToNameFunction
                .transform(this.groupManager.getUsersInGroup(targetGroup));
            conditions.add(new EntityExpr("author", IN, usersNames));
        } else {
            conditions.add(new EntityExpr("author", EQUALS, targetUserName));
        }

        List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);

        log.info("Query returned : " + worklogs.size() + " worklogs");

        Iterator<GenericValue> worklogsIterator = worklogs.iterator();
        while (worklogsIterator.hasNext()) {
            GenericValue genericWorklog = worklogsIterator.next();

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

            if ((priority != null) && (priority.length() != 0) && (!issue.getPriority().getName().equals(priority))) {
                continue;
            }

            if ((projectId != null) && (!project.getId().equals(projectId))) {
                continue;
            }

            ApplicationUser workedUser = this.userManager.getUserByName(genericWorklog.getString("author"));

            Date dateCreated = worklog.getStartDate();
            WeekPortletHeader weekDay = new WeekPortletHeader(dateCreated);
            if ((showWeekends != null) && (!showWeekends.booleanValue()) && (weekDay.isNonBusinessDay())) {
                continue;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateCreated);
            cal.set(11, 0);
            cal.set(12, 0);
            cal.set(13, 0);
            cal.set(14, 0);

            Date dateOfTheDay = cal.getTime();
            Long dateCreatedLong = new Long(cal.getTimeInMillis());

            if (!this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, appUser)) {
                continue;
            }

            if (excelView) {
                List<IScnWorklog> issueWorklogs = this.allWorkLogs.get(issue);
                if (issueWorklogs == null) {
                    issueWorklogs = new ArrayList<>();
                    this.allWorkLogs.put(issue, issueWorklogs);
                }
                issueWorklogs.add(worklog);
            } else {
                Map<Date, Long> weekTimeSpents = (Map<Date, Long>) this.weekWorkLogShort.get(issue);
                if (weekTimeSpents == null) {
                    weekTimeSpents = new Hashtable<Date, Long>();
                    this.weekWorkLogShort.put(issue, weekTimeSpents);
                }

                long spent = worklog.getTimeSpent().longValue();
                Long dateSpent = (Long) weekTimeSpents.get(dateOfTheDay);

                if (dateSpent != null) {
                    spent += dateSpent.longValue();
                }

                weekTimeSpents.put(dateOfTheDay, new Long(spent));

                updateUserWorkLog(worklog, workedUser, dateOfTheDay);

                Map<Date, Long> projectWorkLog = (Map<Date, Long>) this.projectTimeSpents.get(project);
                if (projectWorkLog == null) {
                    projectWorkLog = new Hashtable<Date, Long>();
                    this.projectTimeSpents.put(project, projectWorkLog);
                }

                spent = worklog.getTimeSpent().longValue();

                Long projectSpent = (Long) projectWorkLog.get(dateOfTheDay);

                if (projectSpent != null) {
                    spent += projectSpent.longValue();
                }

                projectWorkLog.put(dateOfTheDay, new Long(spent));

                calculateTimesForProjectGroupedByField(groupByField, worklog, issue, project, dateOfTheDay, formatter);

                spent = worklog.getTimeSpent().longValue();
                dateSpent = (Long) this.weekTotalTimeSpents.get(dateCreatedLong);
                if (dateSpent != null) {
                    spent += dateSpent.longValue();
                }
                this.weekTotalTimeSpents.put(dateCreatedLong, new Long((int) spent));

                spent = worklog.getTimeSpent().longValue();
                if ((showUsers != null) && (showUsers.booleanValue())) {
                    Map<Issue, Map<IScnWorklog, Long>> userWorkLog = this.weekWorkLog
                        .get(workedUser);
                    if (userWorkLog == null) {
                        userWorkLog = new TreeMap<>(new IssueProjectComparator());
                        this.weekWorkLog.put(workedUser, userWorkLog);
                    }
                    Map<IScnWorklog, Long> issueWorkLog = userWorkLog.get(issue);

                    if (issueWorkLog == null) {
                        issueWorkLog = new Hashtable<>();
                        userWorkLog.put(issue, issueWorkLog);
                    }
                    issueWorkLog.put(worklog, new Long(spent));

                    spent = worklog.getTimeSpent().longValue();
                    Map<Issue, Long> issueTotalTimeSpents = this.userTotalTimeSpents.get(workedUser);
                    if (issueTotalTimeSpents == null) {
                        issueTotalTimeSpents = new TreeMap<Issue, Long>(new IssueKeyComparator());
                        this.userTotalTimeSpents.put(workedUser, issueTotalTimeSpents);
                    }
                    Long issueSpent = issueTotalTimeSpents.get(issue);
                    if (issueSpent != null) {
                        spent += issueSpent.longValue();
                    }
                    issueTotalTimeSpents.put(issue, new Long(spent));
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
            if ((calendarDate.get(5) == Calendar.getInstance().get(5))
                && (calendarDate.get(2) == Calendar.getInstance().get(2))
                && (calendarDate.get(1) == Calendar.getInstance().get(1))) {
                businessDay = "toDay";
            } else if (wph.isNonBusinessDay() == true) {
                businessDay = "nonBusinessDay";
            }

            wph.setWeekDayCSS(businessDay);

            if ((showWeekends == null) || (showWeekends.booleanValue()) || (!wph.isNonBusinessDay())) {
                this.weekDays.add(wph);
            }
            calendarDate.add(6, 1);
        }
    }

    private void updateUserWorkLog(IScnWorklog worklog, ApplicationUser workedUser, Date dateOfTheDay) {
        Map<Date, Long> dateToWorkMap = this.userWorkLogShort.get(workedUser);
        if (dateToWorkMap == null) {
            dateToWorkMap = new HashMap<>();
            this.userWorkLogShort.put(workedUser, dateToWorkMap);
        }

        long spent = worklog.getTimeSpent().longValue();
        Long dateSpent = (Long) dateToWorkMap.get(dateOfTheDay);

        if (dateSpent != null) {
            spent += dateSpent.longValue();
        }
        dateToWorkMap.put(dateOfTheDay, Long.valueOf(spent));
    }

    private void calculateTimesForProjectGroupedByField(String groupByFieldID, IScnWorklog worklog, Issue issue,
                                                        Project project, Date dateOfTheDay, DateTimeFormatter formatter) {
        if (groupByFieldID == null) {
            return;
        }
        String fieldValue = TextUtil.getFieldValue(groupByFieldID, issue, formatter);

        Map<String, Map<Date, Long>> projectToFieldWorkLog = this.projectGroupedByFieldTimeSpents
            .get(project);

        if (projectToFieldWorkLog == null) {
            projectToFieldWorkLog = new Hashtable<>();
            this.projectGroupedByFieldTimeSpents.put(project, projectToFieldWorkLog);
        }

        Map<Date, Long> fieldToTimeWorkLog = projectToFieldWorkLog.get(fieldValue);

        if (fieldToTimeWorkLog == null) {
            fieldToTimeWorkLog = new Hashtable<>();
            projectToFieldWorkLog.put(fieldValue, fieldToTimeWorkLog);
        }

        long spent = worklog.getTimeSpent();
        Long projectGroupedSpent = fieldToTimeWorkLog.get(dateOfTheDay);

        if (projectGroupedSpent != null) {
            spent += projectGroupedSpent;
        }

        fieldToTimeWorkLog.put(dateOfTheDay, spent);
    }

    public String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView)
        throws Exception {
        ApplicationUser remoteUser = action.getLoggedInUser();
        I18nBean i18nBean = new I18nBean(remoteUser);

        Date endDate = Pivot.getEndDate(params, formatter);
        Date startDate = Pivot.getStartDate(params, formatter, endDate);

        ApplicationUser targetUser = ParameterUtils.getUserParam(params, "targetUser");
        String priority = ParameterUtils.getStringParam(params, "priority");
        String targetGroup = ParameterUtils.getStringParam(params, "targetGroup");

        Long projectId = null;
        if (!"".equals(ParameterUtils.getStringParam(params, "project"))) {
            projectId = ParameterUtils.getLongParam(params, "project");
        }

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
        if (targetUser == null) {
            targetUser = remoteUser;
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

        if (remoteUser != null) {
            getTimeSpents(remoteUser, startDate, endDate, targetUser.getName(), excelView, priority, targetGroup,
                projectId, filterId, showWeekends, showUsers, groupByField, formatter);
        }

        Map<String, Object> velocityParams = new HashMap<>();
        velocityParams.put("startDate", startDate);
        velocityParams.put("endDate", endDate);
        velocityParams.put("weekDays", this.weekDays);
        velocityParams.put("showUsers", showUsers);

        if (excelView) {
            velocityParams.put("allWorkLogs", this.allWorkLogs);
        } else {
            if (showUsers)
                velocityParams.put("weekWorkLog", this.weekWorkLog);
            else {
                velocityParams.put("weekWorkLog", this.weekWorkLogShort);
            }
            velocityParams.put("weekTotalTimeSpents", this.weekTotalTimeSpents);
            velocityParams.put("userTotalTimeSpents", this.userTotalTimeSpents);
            velocityParams.put("projectTimeSpents", this.projectTimeSpents);
            velocityParams.put("projectGroupedTimeSpents", this.projectGroupedByFieldTimeSpents);
        }
        velocityParams.put("groupByField", groupByField);
        velocityParams.put("formatter", formatter);
        velocityParams.put("fieldVisibility", this.fieldVisibilityManager);
        velocityParams.put("textUtil", new TextUtil(i18nBean));

        return this.descriptor.getHtml((excelView) ? "excel" : "view", velocityParams);
    }

    @Override
    public void validate(ProjectActionSupport action, Map params) {
    }

    @Override
    public boolean isExcelViewSupported() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        return generateReport(action, params, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception {
        return generateReport(action, params, true);
    }
}
