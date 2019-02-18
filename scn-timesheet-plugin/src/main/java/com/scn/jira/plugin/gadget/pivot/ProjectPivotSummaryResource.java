package com.scn.jira.plugin.gadget.pivot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.util.SettingsManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.DateFieldFormatImpl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import com.scn.jira.plugin.gadget.rest.error.ErrorCollection;
import com.scn.jira.plugin.gadget.timesheet.TimeSheetRepresentation;
import com.scn.jira.plugin.report.pivot.Pivot;
import com.scn.jira.util.CalendarUtil;
import com.scn.jira.util.TextUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Named
@Path("/project-pivot-summary")
public class ProjectPivotSummaryResource
{
	private static final Logger log = Logger.getLogger(ProjectPivotSummaryResource.class);

	private final VisibilityValidator visibilityValidator;
	private final ApplicationProperties applicationProperties;
	private final ProjectManager projectManager;
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
	private final SettingsManager scnGlobalPermissionManager;

	@Autowired
	public ProjectPivotSummaryResource(@ComponentImport JiraAuthenticationContext authenticationContext,
			   @ComponentImport PermissionManager permissionManager, @ComponentImport ApplicationProperties applicationProperties,
			   @ComponentImport OutlookDateManager outlookDateManager, @ComponentImport IssueManager issueManager,
			   @ComponentImport SearchProvider searchProvider, @ComponentImport VisibilityValidator visibilityValidator,
			   @ComponentImport FieldVisibilityManager fieldVisibilityManager, @ComponentImport ProjectManager projectManager,
			   @ComponentImport SearchRequestManager searchRequestManager, @ComponentImport GroupManager groupManager,
			   @ComponentImport ProjectRoleManager projectRoleManager, SettingsManager globalSettingsManager)
	{
		this.authenticationContext = authenticationContext;
		this.permissionManager = permissionManager;
		this.applicationProperties = applicationProperties;
		this.outlookDateManager = outlookDateManager;
		this.issueManager = issueManager;
		this.searchProvider = searchProvider;
		this.visibilityValidator = visibilityValidator;
		this.fieldVisibilityManager = fieldVisibilityManager;
		this.projectManager = projectManager;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.projectRoleManager = projectRoleManager;
		this.fFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
		this.scnGlobalPermissionManager = globalSettingsManager;
	}
	
	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getSummary(@Context HttpServletRequest request, @QueryParam("numOfWeeks") int numOfWeeks,
			@QueryParam("reportingDay") int reportingDay, @QueryParam("projectKey") String projectKey,
			@QueryParam("filterId") long filterId, @QueryParam("targetGroup") String targetGroupName)
	{
		VelocityManager vm = ComponentAccessor.getVelocityManager();
		try
		{
			return Response
					.ok(new TimeSheetRepresentation(vm.getBody("templates/scn/pivotgadget/", "project-pivot-summary.vm",
							getVelocityParams(request, numOfWeeks, reportingDay, projectKey, filterId, targetGroupName))))
					.cacheControl(getNoCacheControl()).build();
		}
		catch (VelocityException e)
		{
			e.printStackTrace();
		}
		return Response.serverError().build();
	}
	
	protected Map<String, Object> getVelocityParams(HttpServletRequest request, int numOfWeeks, int reportingDay,
			String groupName, long filterId, String targetGroup)
	{
		Map<String, Object> params = getVelocityParams(numOfWeeks, reportingDay, groupName, filterId, targetGroup);
		params.put("i18n", this.authenticationContext.getI18nHelper());
		params.put("textutils", new TextUtils());
		params.put("req", request);
		VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(this.applicationProperties)
				.getJiraVelocityRequestContext();
		params.put("baseurl", velocityRequestContext.getBaseUrl());
		params.put("requestContext", velocityRequestContext);
		return params;
	}
	
	protected Map<String, Object> getVelocityParams(int numOfWeeks, int reportingDay, String projectKey, long filterId,
			String targetGroup)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		ApplicationUser user = this.authenticationContext.getUser();
		
		params.put("loggedin", Boolean.valueOf(user != null));
		
		if (user == null)
		{
			return params;
		}
		
		I18nBean i18nBean = new I18nBean(user);
		
		Calendar[] dates = CalendarUtil.getDatesRange(reportingDay, numOfWeeks);
		Calendar startDate = dates[0];
		Calendar endDate = dates[1];
		try
		{
			Project project = null;
			if ((filterId == 0L) && (projectKey != null) && (projectKey.length() != 0))
			{
				project = this.projectManager.getProjectObjByKey(projectKey);
				if (project == null)
				{
					log.error("Can't find specified project by key: " + projectKey);
					return params;
				}
				
			}
			
			Pivot pivot = new Pivot(this.authenticationContext, this.outlookDateManager, this.permissionManager,
					this.issueManager, this.searchProvider, this.fieldVisibilityManager, this.searchRequestManager,
                    this.groupManager, this.projectRoleManager, this.scnGlobalPermissionManager);
			
			pivot.getTimeSpents(user, startDate.getTime(), endDate.getTime(), (project != null) ? project.getId() : null,
					(filterId == 0L) ? null : Long.valueOf(filterId), targetGroup, false);
			
			DateFieldFormat dateFieldFormat = new DateFieldFormatImpl(this.fFactory);
			
			params.put("filter", pivot.filter);
			params.put("project", project);
			params.put("startDate", dateFieldFormat.formatDatePicker(startDate.getTime()));
			params.put("endDate", dateFieldFormat.formatDatePicker(endDate.getTime()));
			params.put("outlookDate", this.outlookDateManager.getOutlookDate(i18nBean.getLocale()));
			
			params.put("fieldVisibility", this.fieldVisibilityManager);
			params.put("textUtil", new TextUtil(i18nBean));
			params.put("workedIssues", pivot.workedIssues);
			params.put("workedUsers", pivot.workedUsers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return params;
	}
	
	@GET
	@Path("/validate")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response validate(@QueryParam("num_weeks") String num_weeks,
			@QueryParam("reporting_day") String reporting_day,
			@QueryParam("project_key") String project_key,
			@QueryParam("filter_id") String filter_id,
			@QueryParam("group") String group)
	{
		ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder();
		if (StringUtils.isBlank(num_weeks))
		{
			errorBuilder.addError("num_weeks", "scn.gadget.error.num_weeks.empty", new String[0]);
		}
		if (StringUtils.isBlank(filter_id))
		{
			errorBuilder.addError("filter_id", "scn.gadget.error.filter_id.empty", new String[0]);
		}
		ErrorCollection errorCollection = errorBuilder.build();
		if (!errorCollection.hasAnyErrors())
		{
			return Response.ok().cacheControl(getNoCacheControl()).build();
		}
		return Response.status(400).entity(errorCollection).cacheControl(getNoCacheControl()).build();
	}
	
	private CacheControl getNoCacheControl()
	{
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}