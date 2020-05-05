package com.scn.jira.plugin.report.pivot;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.search.*;
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
import com.atlassian.jira.web.bean.PagerFilter;
import com.scn.jira.util.*;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Named;
import java.sql.Timestamp;
import java.util.*;

import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static org.ofbiz.core.entity.EntityOperator.*;

@Named
@SuppressWarnings("rawtypes")
public class Pivot extends AbstractReport {
	private static final Logger log = Logger.getLogger(Pivot.class);

	private final JiraAuthenticationContext authenticationContext;
	private final PermissionManager permissionManager;
	private final IssueManager issueManager;
	private final SearchProvider searchProvider;
	private final FieldVisibilityManager fieldVisibilityManager;
	private final SearchRequestManager searchRequestManager;
	private final GroupManager groupManager;
	private final ProjectRoleManager projectRoleManager;
    private final IGlobalSettingsManager scnGlobalPermissionManager;
	private final DateTimeFormatter formatter;

	private Date startDate;
	private Date endDate;

	private Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<>();
	@SuppressWarnings("unchecked")
	public Map<Issue, Map<MyUser, Long>> workedIssues = new TreeMap<Issue, Map<MyUser, Long>>(new IssueKeyComparator());
	public Map<MyUser, Long> workedUsers = new TreeMap<>(new MyFullNameComparator());
	public SearchRequest filter = null;

	@Autowired
	public Pivot(JiraAuthenticationContext authenticationContext,
			PermissionManager permissionManager, IssueManager issueManager,
			SearchProvider searchProvider,
			FieldVisibilityManager fieldVisibilityManager,
			SearchRequestManager searchRequestManager, GroupManager groupManager,
			ProjectRoleManager projectRoleManager,
			IGlobalSettingsManager globalSettingsManager) {
		this.authenticationContext = authenticationContext;
		this.permissionManager = permissionManager;
		this.issueManager = issueManager;
		this.searchProvider = searchProvider;
		this.fieldVisibilityManager = fieldVisibilityManager;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.projectRoleManager = projectRoleManager;
        DateTimeFormatterFactory fFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
		this.scnGlobalPermissionManager = globalSettingsManager;
		this.formatter = fFactory.formatter().forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.DATE_PICKER);
	}

	@Override
	public boolean showReport() {
		final ApplicationUser user = this.authenticationContext.getLoggedInUser();
		return this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, user);
	}

	@Override
	public boolean isExcelViewSupported() {
		return true;
	}

	public void getTimeSpents(ApplicationUser remoteUser, Date startDate, Date endDate, Long projectId, Long filterId,
			String targetGroup, boolean excelView) throws SearchException {
        if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, remoteUser)) {
			return;
		}

		Set<Long> filteredIssues = new TreeSet<>();
		if (filterId != null) {
			log.info("Using filter: " + filterId);

			this.filter = this.searchRequestManager.getSearchRequestById(remoteUser, filterId);

			if (this.filter == null)
				return;

			SearchQuery searchQuery = SearchQuery.create(this.filter.getQuery(), remoteUser);
			SearchResults<DocumentWithId> issues = this.searchProvider.search(searchQuery,
					PagerFilter.getUnlimitedFilter());
			for (Object result : issues.getResults()) {
				if (result instanceof Issue) {
					filteredIssues.add(((Issue) result).getId());
				}
			}
		}

		List<EntityCondition> conditions = new ArrayList<>();
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		if (StringUtils.isNotEmpty(targetGroup)) {
			Collection<String> usersNames = UserToNameFunction.transform(groupManager.getUsersInGroup(targetGroup));
			conditions.add(new EntityExpr("author", IN, usersNames));
			log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "', by group '"
					+ targetGroup + "'");
		} else {
			log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "'");
		}

		List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);

		log.info("Query returned : " + worklogs.size() + " worklogs");
        for (GenericValue genericWorklog : worklogs) {
            // Worklog worklog = WorklogUtil.convertToWorklog(genericWorklog,
            // this.worklogManager, this.issueManager);
            Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("issue"));
            final IScnWorklog worklog = WorklogUtil.convertToWorklog(this.projectRoleManager, genericWorklog, issue);

            if ((issue != null) && (((projectId == null) || (projectId.equals(Objects.requireNonNull(issue.getProjectObject()).getId()))))
                && (((filterId == null) || (filteredIssues.contains(issue.getId()))))
                && (this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, remoteUser))) {
                if (excelView) {
                    List<IScnWorklog> issueWorklogs = this.allWorkLogs.computeIfAbsent(issue, k -> new ArrayList<>());
                    issueWorklogs.add(worklog);
                } else {
                    Map<MyUser, Long> issueWorkLog = this.workedIssues.computeIfAbsent(issue, k -> new Hashtable<>());
                    MyUser user;
                    if (worklog.getAuthorKey() != null) {
                        User osuser = Objects.requireNonNull(ComponentAccessor.getUserManager().getUserByKey(worklog.getAuthorKey()))
                            .getDirectoryUser();

                        user = new MyUser(osuser.getName(), osuser.getDisplayName());
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

	private String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView)
			throws Exception {
		ApplicationUser remoteUser = action.getLoggedInUser();
		I18nBean i18nBean = new I18nBean(remoteUser);
		Long projectId = ParameterUtils.getLongParam(params, "projectid");
		Long filterId = ParameterUtils.getLongParam(params, "filterid");
		String targetGroup = ParameterUtils.getStringParam(params, "targetGroup");
		if (excelView) {
			validate(action, params);
		}
		getTimeSpents(remoteUser, startDate, endDate, projectId, filterId, targetGroup, excelView);

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

	public static Date getStartDate(Map params, DateTimeFormatter formatter, Date endDate)
			throws IllegalArgumentException {
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
