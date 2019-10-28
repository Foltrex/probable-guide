package com.scn.jira.plugin.gadget.timesheet;

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

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.plugin.report.timesheet.TimeSheet;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.VelocityException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverter;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverterImpl;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.DateFieldFormatImpl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import com.scn.jira.plugin.gadget.rest.error.ErrorCollection;
import com.scn.jira.util.CalendarUtil;
import com.scn.jira.util.ServletUtil;
import com.scn.jira.util.TextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Named
@Path("/timesheet")
public class TimeSheetResource {
	private IssueManager issueManager;
	private VisibilityValidator visibilityValidator;
	private JiraAuthenticationContext authenticationContext;
	private PermissionManager permissionManager;
	private ApplicationProperties applicationProperties;
	private SearchProvider searchProvider;
	private final UserManager userManager;
	private final DateTimeFormatterFactory fFactory;
	private final ProjectRoleManager projectRoleManager;
	private final GroupManager groupManager;
	private final SearchRequestManager searchRequestManager;
	private final GlobalSettingsManager scnPermissionManager;
	private final FieldVisibilityManager fieldVisibilityManager;

	@Autowired
	public TimeSheetResource(@ComponentImport JiraAuthenticationContext authenticationContext,
			@ComponentImport PermissionManager permissionManager,
			@ComponentImport ApplicationProperties applicationProperties, @ComponentImport IssueManager issueManager,
			@ComponentImport SearchProvider searchProvider, @ComponentImport VisibilityValidator visibilityValidator,
			@ComponentImport UserManager userManager, @ComponentImport SearchRequestManager searchRequestManager,
			@ComponentImport GroupManager groupManager, @ComponentImport ProjectRoleManager projectRoleManager,
			@Qualifier("globalSettingsManager") GlobalSettingsManager globalSettingsManager,
			@ComponentImport FieldVisibilityManager fieldVisibilityManager) {
		this.authenticationContext = authenticationContext;
		this.permissionManager = permissionManager;
		this.applicationProperties = applicationProperties;
		this.issueManager = issueManager;
		this.visibilityValidator = visibilityValidator;
		this.searchProvider = searchProvider;
		this.fFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
		this.userManager = userManager;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.projectRoleManager = projectRoleManager;
		this.scnPermissionManager = globalSettingsManager;
		this.fieldVisibilityManager = fieldVisibilityManager;
	}

	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("targetUser") String targetUserName) {
		int numOfWeeks = ServletUtil.getIntParam(request, "numOfWeeks", 1);

		int reportingDay = ServletUtil.getIntParam(request, "reportingDay", 2);

		ApplicationUser targetUser = this.authenticationContext.getLoggedInUser();
		if ((targetUserName != null) && (targetUserName.length() != 0)) {
			targetUser = ComponentAccessor.getUserManager().getUserByName(targetUserName);
		}

		VelocityManager vm = ComponentAccessor.getVelocityManager();
		try {
			return Response
					.ok(new TimeSheetRepresentation(vm.getBody("templates/scn/timesheetportlet/",
							"timesheet-portlet.vm",
							getVelocityParams(request, numOfWeeks, reportingDay, targetUser.getDirectoryUser()))))
					.cacheControl(getNoCacheControl()).build();
		} catch (VelocityException e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

	private Map<String, Object> getVelocityParams(HttpServletRequest request, int numOfWeeks, int reportingDay,
			User targetUser) {
		Map<String, Object> params = getVelocityParams(numOfWeeks, reportingDay, targetUser);
		params.put("i18n", this.authenticationContext.getI18nHelper());
		params.put("textutils", new TextUtils());
		params.put("req", request);
		VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(
				this.applicationProperties).getJiraVelocityRequestContext();
		params.put("baseurl", velocityRequestContext.getBaseUrl());
		params.put("requestContext", velocityRequestContext);
		return params;
	}

	private Map<String, Object> getVelocityParams(int numOfWeeks, int reportingDay, User targetUser) {
		Map<String, Object> params = new HashMap<String, Object>();
		ApplicationUser user = this.authenticationContext.getLoggedInUser();

		params.put("loggedin", Boolean.valueOf(user != null));

		if (user == null) {
			return params;
		}

		I18nBean i18nBean = new I18nBean(user);

		Calendar[] dates = CalendarUtil.getDatesRange(reportingDay, numOfWeeks);
		Calendar startDate = dates[0];
		Calendar endDate = dates[1];
		try {
			params.put("targetUser", targetUser);

			TimeSheet ts = new TimeSheet(this.permissionManager, this.issueManager, this.searchProvider,
					this.visibilityValidator, this.userManager, this.searchRequestManager, this.groupManager,
					this.projectRoleManager, this.scnPermissionManager, this.fieldVisibilityManager);

			ts.getTimeSpents(user, startDate.getTime(), endDate.getTime(), targetUser.getName(), false, null, null,
					null, null, null, null, null, null);

			params.put("weekDays", ts.getWeekDays());
			params.put("weekWorkLog", ts.getWeekWorkLogShort());
			params.put("weekTotalTimeSpents", ts.getWeekTotalTimeSpents());
			params.put("fieldVisibility", this.fieldVisibilityManager);
			DatePickerConverter dpc = new DatePickerConverterImpl(this.authenticationContext,
					new DateFieldFormatImpl(this.fFactory));
			params.put("dpc", dpc);
			params.put("startDate", startDate.getTime());
			endDate.add(6, -1);

			params.put("endDate", endDate.getTime());
			params.put("textUtil", new TextUtil(i18nBean));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return params;
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}

	@GET
	@Path("/validate")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response validate(@QueryParam("num_weeks") String num_weeks, @QueryParam("user") String user,
			@QueryParam("reporting_day") String reporting_day) {
		ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder();
		if (StringUtils.isBlank(num_weeks)) {
			errorBuilder.addError("num_weeks", "scn.gadget.error.num_weeks.empty", new String[0]);
		}
		ErrorCollection errorCollection = errorBuilder.build();
		if (!errorCollection.hasAnyErrors()) {
			return Response.ok().cacheControl(getNoCacheControl()).build();
		}
		return Response.status(400).entity(errorCollection).cacheControl(getNoCacheControl()).build();
	}
}