package com.scn.jira.mytime.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.mytime.manager.MyTimeManager;
import com.scn.jira.mytime.representation.MyTimeRepresentation;
import org.apache.velocity.exception.VelocityException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.velocity.VelocityManager;
import com.scn.jira.mytime.representation.WeekRepresentation;
import com.scn.jira.mytime.util.DateUtils;
import com.scn.jira.mytime.util.ServletUtil;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/timeobj")
@Named
public class MyTimeResource {
	private JiraAuthenticationContext authenticationContext;
	private MyTimeManager myTimeManager;

	@Inject
	public MyTimeResource(@ComponentImport IssueManager issueManager, @ComponentImport ProjectRoleManager projectRoleManager,
						  @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
						  @ComponentImport JiraAuthenticationContext jiraAuthenticationContext) {
		super();
		this.authenticationContext = jiraAuthenticationContext;
		this.myTimeManager = new MyTimeManager(issueManager, projectRoleManager, overridedWorklogManager, authenticationContext.getI18nHelper());
	}
	
	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
	
	/**
	 * Returns the list of projects browsable by the user in the specified
	 * request.
	 * 
	 * @param request
	 *            the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("viewType") String viewType) {
		System.out.println("Hi!!! I am the gadget");
		
		// Logged in user
//		User user = getUser(request);
		
		VelocityManager vm = ComponentAccessor.getVelocityManager();
		try {
			String vmName = "mytime.vm";
			
			return Response.ok(new MyTimeRepresentation(vm.getBody("template/", vmName, getVelocityParams(request, viewType))))
					.cacheControl(getNoCacheControl()).build();
		} catch (VelocityException e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}
	
	private Map getVelocityParams(HttpServletRequest request, String viewType) {
		int currentPeriod = ServletUtil.getIntParam(request, "currentPeriod", 0);
		int currentslideStep = ServletUtil.getIntParam(request, "slideStep", 0);

		User user = getUser(request);
		
		Map params = getVelocityParams(user, viewType, currentPeriod, currentslideStep);
		
		params.put("i18n", this.authenticationContext.getI18nHelper().getLocale().getDisplayName());
		params.put("req", request);
		return params;
	}
	
	private Map getVelocityParams(User loggeduser, String viewType, int currentperiod, int currentslideStep) {
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("loggedin", true);
		
		Date date = new Date();
		
		Date startDate = DateUtils.getWeekStartDate(0, date);
		Date endDate = DateUtils.getWeekEndDate(0, date);
		
		int slide = currentslideStep;
		Date nameDate = startDate;
		if (viewType != null && viewType.equals("Monthly view")) {
			// if period was switched we need to recount slideStep from Monthly
			// to weekly
			if (currentperiod == 1) {
				Date startDateMonth = DateUtils.getMonthStartDate(currentslideStep, date);
				
				startDate = DateUtils.getWeekStartDate(0, startDateMonth);
				endDate = DateUtils.getWeekEndDate(0, startDateMonth);
				slide = DateUtils.getWeekSlide(date, startDateMonth);
			}
			else {
				startDate = DateUtils.getWeekStartDate(currentslideStep, date);
				endDate = DateUtils.getWeekEndDate(currentslideStep, date);
			}
			nameDate = startDate;
		}
		else {
			if (currentperiod == 1) {
				Date startDateWeek = DateUtils.getWeekEndDate(currentslideStep, date);
				System.out.println("startDateWeek "+startDateWeek);
				System.out.println("currentslideStep "+currentslideStep);
				System.out.println("date "+date);
				nameDate = DateUtils.getMonthStartDate(0, startDateWeek);
				System.out.println("nameDate "+nameDate);
				startDate = DateUtils.getMonthStartDateFromWeekStart(0, startDateWeek);
				System.out.println("startDate "+startDate);
				endDate = DateUtils.getMonthEndDateToWeekEnd(0, startDateWeek);
				System.out.println("endDate "+endDate);
				slide = DateUtils.getMonthSlide(date, startDateWeek);
				System.out.println("slide "+slide);
			}
			else {
				nameDate = DateUtils.getMonthStartDate(currentslideStep, date);
				startDate = DateUtils.getMonthStartDateFromWeekStart(currentslideStep, date);
				endDate = DateUtils.getMonthEndDateToWeekEnd(currentslideStep, date);
			}
			
		}

		String reportView = (viewType != null && viewType.equals("Monthly view")) ? "week" : "month";
		List<WeekRepresentation> weeksToRepresent = myTimeManager.getWeeksRepresentation(loggeduser.getName(), startDate, endDate,reportView);
		
		List<String> days = new ArrayList<String>(Arrays.asList("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"));
		
		reportView="month";
		
		params.put("reportView", reportView);
		
		params.put("slideStep", slide);
		
		params.put("userName", "paradinets");
		
		params.put("days", days);
		
		params.put("slidePeriod", DateUtils.formSlidePeriod(nameDate, endDate, viewType));
		
		params.put("weeksToRepresent", weeksToRepresent);
		
		return params;
	}

	private User getUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDirectoryUser(); // TODO should be re-factored
	}
	
}
