package com.scn.jira.plugin.report.pivot;

import static com.scn.jira.worklog.globalsettings.IGlobalSettingsManager.SCN_TIMETRACKING;
import static com.scn.jira.worklog.core.scnwl.IScnWorklogStore.SCN_WORKLOG_ENTITY;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

import java.sql.Timestamp;
import java.util.*;

import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.util.MyFullNameComparator;
import com.scn.jira.util.MyUser;
import com.scn.jira.util.TextUtil;
import com.scn.jira.util.UserToNameFunction;
import com.scn.jira.util.WorklogUtil;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Named;

@Named
public class Pivot extends AbstractReport
{
	private static final Logger log = Logger.getLogger(Pivot.class);
	
	private final JiraAuthenticationContext authenticationContext;
	private final OutlookDateManager outlookDateManager;
	private final PermissionManager permissionManager;
	private final IssueManager issueManager;
	private final SearchProvider searchProvider;
	private final FieldVisibilityManager fieldVisibilityManager;
	private final SearchRequestManager searchRequestManager;
	private final GroupManager groupManager;
	private final ProjectRoleManager projectRoleManager;
	private final DateTimeFormatterFactory fFactory;
	private final GlobalSettingsManager scnGlobalPermissionManager;
	
	private Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<Issue, List<IScnWorklog>>();
	
	public Map<Issue, Map<MyUser, Long>> workedIssues = new TreeMap<Issue, Map<MyUser, Long>>(new IssueKeyComparator());
	
	public Map<MyUser, Long> workedUsers = new TreeMap<MyUser, Long>(new MyFullNameComparator());
	
	public SearchRequest filter = null;

	@Autowired
	public Pivot(@ComponentImport JiraAuthenticationContext authenticationContext, @ComponentImport OutlookDateManager outlookDateManager,
			 @ComponentImport PermissionManager permissionManager, @ComponentImport IssueManager issueManager,
			 @ComponentImport SearchProvider searchProvider, @ComponentImport FieldVisibilityManager fieldVisibilityManager,
			 @ComponentImport SearchRequestManager searchRequestManager, @ComponentImport GroupManager groupManager,
			 @ComponentImport ProjectRoleManager projectRoleManager, @Qualifier("globalSettingsManager") GlobalSettingsManager globalSettingsManager)
	{
		this.authenticationContext = authenticationContext;
		this.outlookDateManager = outlookDateManager;
		this.permissionManager = permissionManager;
		this.issueManager = issueManager;
		this.searchProvider = searchProvider;
		this.fieldVisibilityManager = fieldVisibilityManager;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.projectRoleManager = projectRoleManager;
		this.fFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
		this.scnGlobalPermissionManager = globalSettingsManager;
	}
	
	@Override
	public boolean showReport()
	{
		final ApplicationUser user = this.authenticationContext.getUser();
		return this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, user);
	}
	
	public boolean isExcelViewSupported()
	{
		return true;
	}
	
	public void getTimeSpents(ApplicationUser remoteUser, Date startDate, Date endDate, Long projectId, Long filterId, String targetGroup,
			boolean excelView)
			throws SearchException, GenericEntityException
	{
		ApplicationUser appUser = remoteUser;
		if (!this.scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, appUser)){
			return;
		}
		
		Set<Long> filteredIssues = new TreeSet<Long>();
		if (filterId != null)
		{
			log.info("Using filter: " + filterId);
			
			this.filter = this.searchRequestManager.getSearchRequestById(appUser, filterId);
			
			if (this.filter == null) return;

			SearchQuery searchQuery = SearchQuery.create(this.filter.getQuery(), remoteUser);
			SearchResults issues = this.searchProvider.search(searchQuery, PagerFilter.getUnlimitedFilter());
			for (Object result : issues.getResults()) {
				if (result instanceof Issue) {
					filteredIssues.add(((Issue)result).getId());
				}
			}
		}
		
		List<EntityCondition> conditions = new ArrayList<EntityCondition>();
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		if (StringUtils.isNotEmpty(targetGroup))
		{
			Collection<String> usersNames = UserToNameFunction.transform(groupManager.getUsersInGroup(targetGroup));
			conditions.add(new EntityExpr("author", IN, usersNames));
			
			log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "', by group '" + targetGroup + "'");
		}
		else
		{
			log.info("Searching worklogs created since '" + startDate + "', till '" + endDate + "'");
		}
		
		List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd(SCN_WORKLOG_ENTITY, conditions);
		
		log.info("Query returned : " + worklogs.size() + " worklogs");
		Iterator<GenericValue> worklogsIterator = worklogs.iterator();
		while (worklogsIterator.hasNext())
		{
			GenericValue genericWorklog = (GenericValue) worklogsIterator.next();
			
			// Worklog worklog = WorklogUtil.convertToWorklog(genericWorklog, this.worklogManager, this.issueManager);
			Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("issue"));
			final IScnWorklog worklog = WorklogUtil.convertToWorklog(this.projectRoleManager, genericWorklog, issue);
			
			if ((issue != null) && (((projectId == null) || (projectId.equals(issue.getProjectObject().getId()))))
					&& (((filterId == null) || (filteredIssues.contains(issue.getId())))) &&
					(this.permissionManager.hasPermission(10, issue, appUser)))
			{
				if (excelView)
				{
					List<IScnWorklog> issueWorklogs = (List<IScnWorklog>) this.allWorkLogs.get(issue);
					if (issueWorklogs == null)
					{
						issueWorklogs = new ArrayList<IScnWorklog>();
						this.allWorkLogs.put(issue, issueWorklogs);
					}
					issueWorklogs.add(worklog);
				}
				else
				{
					Map<MyUser, Long> issueWorkLog = (Map<MyUser, Long>) this.workedIssues.get(issue);
					if (issueWorkLog == null)
					{
						issueWorkLog = new Hashtable<MyUser, Long>();
						this.workedIssues.put(issue, issueWorkLog);
					}
					MyUser user;
					if (worklog.getAuthorKey() != null)
					{
						User osuser = ComponentAccessor.getUserManager().getUserByKey(worklog.getAuthorKey()).getDirectoryUser();
						
						user = new MyUser(osuser.getName(), osuser.getDisplayName());
					}
					else
					{
						user = new MyUser("anonymous", "anonymous");
					}
					long timespent = worklog.getTimeSpent().longValue();
					Long worked = (Long) issueWorkLog.get(user);
					if (worked != null)
					{
						timespent += worked.longValue();
					}
					
					worked = new Long(timespent);
					issueWorkLog.put(user, worked);
					
					timespent = worklog.getTimeSpent().longValue();
					worked = (Long) this.workedUsers.get(user);
					if (worked != null)
					{
						timespent += worked.longValue();
					}
					worked = new Long(timespent);
					this.workedUsers.put(user, worked);
				}
			}
		}
	}
	
	
	public String generateReport(ProjectActionSupport action, Map<String, Object> params, boolean excelView)
			throws Exception
	{
		ApplicationUser remoteUser = action.getLoggedInApplicationUser();
		I18nBean i18nBean = new I18nBean(remoteUser);
		
		Long projectId = ParameterUtils.getLongParam(params, "projectid");
		
		Long filterId = ParameterUtils.getLongParam(params, "filterid");
		
		Date endDate = getEndDate(params, i18nBean);
		Date startDate = getStartDate(params, i18nBean, endDate);
		String targetGroup = ParameterUtils.getStringParam(params, "targetGroup");
		
		getTimeSpents(remoteUser, startDate, endDate, projectId, filterId, targetGroup, excelView);
		DateTimeFormatter datePickerFormatter = fFactory.formatter().forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.DATE_PICKER);
		
		params.put("startDate", datePickerFormatter.format(startDate));
		Calendar calendarDate = Calendar.getInstance();
		calendarDate.setTime(endDate);
		
		calendarDate.add(6, -1);
		params.put("endDate", datePickerFormatter.format(calendarDate.getTime()));
		params.put("outlookDate", this.outlookDateManager.getOutlookDate(i18nBean.getLocale()));
		
		params.put("fieldVisibility", this.fieldVisibilityManager);
		params.put("textUtil", new TextUtil(i18nBean));

		if (excelView){
			params.put("allWorkLogs", this.allWorkLogs);
		} else{
			params.put("workedIssues", this.workedIssues);
			params.put("workedUsers", this.workedUsers);
		}
		
		return this.descriptor.getHtml((excelView) ? "excel" : "view", params);
	}
	
	public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception
	{
		return generateReport(action, params, false);
	}
	
	public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception
	{
		return generateReport(action, params, true);
	}
	
	public void validate(ProjectActionSupport action, Map params)
	{
		ApplicationUser remoteUser = action.getLoggedInApplicationUser();
		I18nHelper i18nBean = new I18nBean(remoteUser);
		
		Date startDate = ParameterUtils.getDateParam(params, "startDate", i18nBean.getLocale());
		Date endDate = ParameterUtils.getDateParam(params, "endDate", i18nBean.getLocale());
		
		if ((startDate == null) || (endDate == null) || (!endDate.before(startDate)))
			return;
		action.addError("endDate", action.getText("report.pivot.before.startdate"), Reason.VALIDATION_FAILED);
	}
	
	public static Date getEndDate(Map<String, Object> params, I18nBean i18nBean)
	{
		Date endDate = ParameterUtils.getDateParam(params, "endDate", i18nBean.getLocale());
		
		Calendar calendarDate = Calendar.getInstance();
		if (endDate == null)
		{
			calendarDate.set(11, 0);
			calendarDate.set(12, 0);
			calendarDate.set(13, 0);
			calendarDate.set(14, 0);
		}
		else
		{
			calendarDate.setTime(endDate);
			calendarDate.add(6, 1);
		}
		endDate = calendarDate.getTime();
		
		return endDate;
	}
	
	public static Date getStartDate(Map<String, Object> params, I18nBean i18nBean, Date endDate)
	{
		Date startDate = ParameterUtils.getDateParam(params, "startDate", i18nBean.getLocale());
		
		if (startDate == null)
		{
			Calendar calendarDate = Calendar.getInstance();
			calendarDate.setTime(endDate);
			calendarDate.add(3, -1);
			startDate = calendarDate.getTime();
		}
		
		return startDate;
	}
}