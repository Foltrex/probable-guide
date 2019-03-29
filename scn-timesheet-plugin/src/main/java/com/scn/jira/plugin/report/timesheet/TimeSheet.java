package com.scn.jira.plugin.report.timesheet;

import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

import java.sql.Timestamp;
import java.util.*;

import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.util.TextUtil;
import com.scn.jira.util.UserToNameFunction;
import com.scn.jira.util.WeekPortletHeader;
import com.scn.jira.util.WorklogUtil;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.AbstractIssue;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchResults;
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
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.plugin.report.pivot.Pivot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Named;

@Named
public class TimeSheet extends AbstractReport {

	private static final Logger log = Logger.getLogger(TimeSheet.class);

	private final OutlookDateManager outlookDateManager;
	private final PermissionManager permissionManager;
	private final IssueManager issueManager;
	private final GlobalSettingsManager scnGlobalPermissionManager;
	private final UserManager userManager;
	private final SearchProvider searchProvider;
	private final VisibilityValidator visibilityValidator;
	private final ProjectRoleManager projectRoleManager;
	private final GroupManager groupManager;
	private final SearchRequestManager searchRequestManager;
	private final FieldVisibilityManager fieldVisibilityManager; 
	
	private List<WeekPortletHeader> weekDays = new ArrayList<WeekPortletHeader>();
	private Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<Issue, List<IScnWorklog>>();
    private Map<ApplicationUser, Map<Issue, Map<IScnWorklog, Long>>> weekWorkLog = new TreeMap(new UserComparator());

	private Map<Issue, Map<Date,Long>> weekWorkLogShort = new TreeMap<Issue, Map<Date,Long>>(new IssueKeyComparator());
	private Map<ApplicationUser, Map<Date, Long>> userWorkLogShort = new TreeMap(new UserComparator());
	private Map<Long, Long> weekTotalTimeSpents = new Hashtable<Long, Long>();
	private Map<ApplicationUser, Map<Issue, Long>> userTotalTimeSpents = new Hashtable();
	private Map<Project, Map<Date, Long>> projectTimeSpents = new Hashtable<Project, Map<Date, Long>>();

	private Map<Project, Map<String, Map<Date, Long>>> projectGroupedByFieldTimeSpents = new Hashtable<Project, Map<String, Map<Date, Long>>>();

	@Autowired
	public TimeSheet(@ComponentImport OutlookDateManager outlookDateManager, @ComponentImport PermissionManager permissionManager,
			 @ComponentImport IssueManager issueManager, @ComponentImport SearchProvider searchProvider,
			 @ComponentImport VisibilityValidator visibilityValidator, @ComponentImport UserManager userManager,
			 @ComponentImport SearchRequestManager searchRequestManager, @ComponentImport GroupManager groupManager,
			 @ComponentImport ProjectRoleManager projectRoleManager, @Qualifier("globalSettingsManager") GlobalSettingsManager globalSettingsManager,
			 @ComponentImport FieldVisibilityManager fieldVisibilityManager)
	{
		this.outlookDateManager = outlookDateManager;
		this.permissionManager = permissionManager;
		this.issueManager = issueManager;
		this.userManager = userManager;
		this.searchProvider = searchProvider;
		this.visibilityValidator = visibilityValidator;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.projectRoleManager = projectRoleManager;
		this.scnGlobalPermissionManager = globalSettingsManager;
		this.fieldVisibilityManager = fieldVisibilityManager;
	}

	@Override
	public boolean showReport() {
		final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
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
	
	public void getTimeSpents(ApplicationUser appUser, Date startDate, Date endDate, String targetUserName,
			boolean excelView, String priority, String targetGroup, Long projectId, Long filterId,
			Boolean showWeekends, Boolean showUsers, String groupByField, OutlookDate outlookDate)
			throws SearchException, GenericEntityException {

		if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, appUser)) {
			return;
		}

		Set<Long> filteredIssues = new TreeSet<Long>();
		Iterator<Issue> i;
		if (filterId != null) {
			log.info("Using filter: " + filterId);
			SearchRequest filter = this.searchRequestManager.getSearchRequestById(appUser, filterId);
			if (filter != null) {

				SearchQuery searchQuery = SearchQuery.create(filter.getQuery(), appUser);
				SearchResults issues = this.searchProvider.search(searchQuery, PagerFilter.getUnlimitedFilter());
				for (Object result : issues.getResults()) {
					if (result instanceof Issue) {
						filteredIssues.add(((Issue)result).getId());
					}
				}
			}
		}

		List<EntityCondition> conditions = new ArrayList<EntityCondition>();
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		if (StringUtils.isNotEmpty(targetGroup)) {
			Collection<String> usersNames = UserToNameFunction.transform(this.groupManager.getUsersInGroup(targetGroup));
			conditions.add(new EntityExpr("author", IN, usersNames));
		} else {
			conditions.add(new EntityExpr("author", EQUALS, targetUserName));
		}

		List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);

		log.info("Query returned : " + worklogs.size() + " worklogs");

		Iterator<GenericValue> worklogsIterator = worklogs.iterator();
		while (worklogsIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogsIterator.next();

			Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("issue"));
			IScnWorklog worklog = WorklogUtil.convertToWorklog(projectRoleManager, genericWorklog, issue);

			boolean isValidVisibility = this.visibilityValidator.isValidVisibilityData(
					new JiraServiceContextImpl(appUser), "worklog", worklog.getIssue(), worklog.getGroupLevel(), 
					(worklog.getRoleLevelId() != null) ? worklog.getRoleLevelId().toString() : null);

			if (!isValidVisibility) {
				continue;
			}

			if ((filterId != null) && (!filteredIssues.contains(issue.getId()))) {
				continue;
			}

			Project project = issue.getProjectObject();

			if ((priority != null) && (priority.length() != 0)
					&& (!issue.getPriorityObject().getName()/*getString("priority")*/.equals(priority))) {
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

			if (!this.permissionManager.hasPermission(10, issue, appUser)) {
				continue;
			}

			if (excelView) {
				List<IScnWorklog> issueWorklogs = (List<IScnWorklog>) this.allWorkLogs.get(issue);
				if (issueWorklogs == null) {
					issueWorklogs = new ArrayList<IScnWorklog>();
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

				calculateTimesForProjectGroupedByField(groupByField, worklog, issue, project, dateOfTheDay,
						outlookDate);

				spent = worklog.getTimeSpent().longValue();
				dateSpent = (Long) this.weekTotalTimeSpents.get(dateCreatedLong);
				if (dateSpent != null) {
					spent += dateSpent.longValue();
				}
				this.weekTotalTimeSpents.put(dateCreatedLong, new Long((int) spent));

				spent = worklog.getTimeSpent().longValue();
				if ((showUsers != null) && (showUsers.booleanValue())) {
					Map<Issue, Map<IScnWorklog, Long>> userWorkLog = (Map<Issue, Map<IScnWorklog, Long>>) this.weekWorkLog.get(workedUser);
					if (userWorkLog == null) {
						userWorkLog = new TreeMap<Issue, Map<IScnWorklog, Long>>(new IssueProjectComparator());
						this.weekWorkLog.put(workedUser, userWorkLog);
					}
					Map<IScnWorklog, Long> issueWorkLog = (Map<IScnWorklog, Long>) userWorkLog.get(issue);

					if (issueWorkLog == null) {
						issueWorkLog = new Hashtable<IScnWorklog, Long>();
						userWorkLog.put(issue, issueWorkLog);
					}
					issueWorkLog.put(worklog, new Long(spent));

					spent = worklog.getTimeSpent().longValue();
					Map<Issue, Long> issueTotalTimeSpents = (Map<Issue, Long>) this.userTotalTimeSpents.get(workedUser);
					if (issueTotalTimeSpents == null) {
						issueTotalTimeSpents = new TreeMap<Issue, Long>(new IssueKeyComparator());
						this.userTotalTimeSpents.put(workedUser, issueTotalTimeSpents);
					}
					Long issueSpent = (Long) issueTotalTimeSpents.get(issue);
					if (issueSpent != null) {
						spent += issueSpent.longValue();
					}
					issueTotalTimeSpents.put(issue, new Long(spent));
				}
			}

		}

		//I18nBean i18nBean = new I18nBean(remoteUser);

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
		Map<Date, Long> dateToWorkMap = (Map<Date, Long>) this.userWorkLogShort.get(workedUser);
		if (dateToWorkMap == null) {
			dateToWorkMap = new HashMap<Date, Long>();
			this.userWorkLogShort.put(workedUser, dateToWorkMap);
		}

		long spent = worklog.getTimeSpent().longValue();
		Long dateSpent = (Long) dateToWorkMap.get(dateOfTheDay);

		if (dateSpent != null) {
			spent += dateSpent.longValue();
		}
		dateToWorkMap.put(dateOfTheDay, Long.valueOf(spent));
	}

	private void calculateTimesForProjectGroupedByField(String groupByFieldID, IScnWorklog worklog,
			Issue issue, Project project, Date dateOfTheDay, OutlookDate outlookDate) {
		if (groupByFieldID == null) {
			return;
		}
		String fieldValue = TextUtil.getFieldValue(groupByFieldID, issue, outlookDate);

		Map<String, Map<Date, Long>> projectToFieldWorkLog = (Map<String, Map<Date, Long>>) this.projectGroupedByFieldTimeSpents.get(project);

		if (projectToFieldWorkLog == null) {
			projectToFieldWorkLog = new Hashtable<String, Map<Date, Long>>();
			this.projectGroupedByFieldTimeSpents.put(project, projectToFieldWorkLog);
		}

		Map<Date, Long> fieldToTimeWorkLog = (Map<Date, Long>) projectToFieldWorkLog.get(fieldValue);

		if (fieldToTimeWorkLog == null) {
			fieldToTimeWorkLog = new Hashtable<Date, Long>();
			projectToFieldWorkLog.put(fieldValue, fieldToTimeWorkLog);
		}

		long spent = worklog.getTimeSpent().longValue();
		Long projectGroupedSpent = (Long) fieldToTimeWorkLog.get(dateOfTheDay);

		if (projectGroupedSpent != null) {
			spent += projectGroupedSpent.longValue();
		}

		fieldToTimeWorkLog.put(dateOfTheDay, new Long(spent));
	}

	public String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView) throws Exception {
		ApplicationUser remoteUser = action.getLoggedInApplicationUser();
		I18nBean i18nBean = new I18nBean(remoteUser);

		Date endDateTmp = Pivot.getEndDate(params, i18nBean);
		Date startDateTmp = Pivot.getStartDate(params, i18nBean, endDateTmp);

		Date endDate = new Date(endDateTmp.getYear(), endDateTmp.getMonth(), endDateTmp.getDate());
		Date startDate = new Date(startDateTmp.getYear(), startDateTmp.getMonth(), startDateTmp.getDate());

		ApplicationUser targetUser = ParameterUtils.getUserParam(params, "targetUser");
		String priority = ParameterUtils.getStringParam(params, "priority");
		String targetGroup = ParameterUtils.getStringParam(params, "targetGroup");

		Long projectId = null;
		if (!"".equals(ParameterUtils.getStringParam(params, "project"))) {
			projectId = ParameterUtils.getLongParam(params, "project");
		}

		Long filterId = ParameterUtils.getLongParam(params, "filterid");

		Boolean showWeekends = null;
		if ("true".equalsIgnoreCase(ParameterUtils.getStringParam(params, "weekends")))
			showWeekends = new Boolean(true);
		else {
			showWeekends = new Boolean(false);
		}
		Boolean showUsers = null;
		if ("true".equalsIgnoreCase(ParameterUtils.getStringParam(params, "showUsers")))
			showUsers = new Boolean(true);
		else {
			showUsers = new Boolean(false);
		}
		if (targetUser == null) {
			targetUser = (ApplicationUser) remoteUser;
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

		OutlookDate outlookDate = this.outlookDateManager.getOutlookDate(i18nBean.getLocale());

		if (remoteUser != null) {
			getTimeSpents(remoteUser, startDate, endDate, targetUser.getName(), excelView, priority,
					targetGroup, projectId, filterId, showWeekends, showUsers, groupByField, outlookDate);
		}

		Map<String, Object> velocityParams = new HashMap<String, Object>();
		velocityParams.put("startDate", startDate);
		velocityParams.put("endDate", endDate);
		velocityParams.put("weekDays", this.weekDays);
		velocityParams.put("showUsers", showUsers);

		if (excelView) {
			velocityParams.put("allWorkLogs", this.allWorkLogs);
		} else {
			if (showUsers.booleanValue())
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
		velocityParams.put("outlookDate", outlookDate);
		velocityParams.put("fieldVisibility", this.fieldVisibilityManager);
		velocityParams.put("textUtil", new TextUtil(i18nBean));
		
		return this.descriptor.getHtml((excelView) ? "excel" : "view", velocityParams);
	}

	public void validate(ProjectActionSupport action, Map params) {
	}

	public boolean isExcelViewSupported() {
		return true;
	}

	public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
		return generateReport(action, params, false);
	}

	public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception {
		return generateReport(action, params, true);
	}
}