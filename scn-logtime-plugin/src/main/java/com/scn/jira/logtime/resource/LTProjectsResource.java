package com.scn.jira.logtime.resource;

import static com.scn.jira.worklog.globalsettings.GlobalSettingsManager.SCN_TIMETRACKING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.logtime.manager.IWorklogLogtimeManager;
import com.scn.jira.logtime.manager.WicketManager;
import com.scn.jira.logtime.manager.WorklogLogtimeManager;
import com.scn.jira.logtime.representation.LTProjectRepresentation;
import com.scn.jira.logtime.representation.LTProjectsRepresentation;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.ServletUtil;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.velocity.VelocityManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.logtime.representation.ProjectRepresentation;
import com.scn.jira.logtime.representation.WLsTypeRepresentation;
import com.scn.jira.logtime.representation.WeekRepresentation;
import com.scn.jira.logtime.representation.WicketRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/projectsobj")
public class LTProjectsResource {
	private static final Logger LOGGER = Logger.getLogger(LTProjectsResource.class);

	private UserManager userManager;
	private PermissionManager permissionManager;
	private JiraAuthenticationContext authenticationContext;
	private ProjectManager projectManager;
	private ExtendedConstantsManager extendedConstantsManager;
	private IGlobalSettingsManager scnGlobalPermissionManager;
	private IWorklogLogtimeManager iWorklogLogtimeManager;
	private WicketManager wicketManager;

	@Inject
	public LTProjectsResource(@ComponentImport PermissionManager permissionManager,
			@ComponentImport JiraAuthenticationContext authenticationContext,
			@ComponentImport ProjectManager projectManager, @ComponentImport IssueManager issueManager,
			@ComponentImport ProjectRoleManager projectRoleManager,
			@Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
			@ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
			@ComponentImport DefaultScnWorklogManager scnWorklogManager,
			@ComponentImport OfBizScnWorklogStore ofBizScnWorklogStore,
			@ComponentImport ScnProjectSettingsManager projectSettignsManager,
			@ComponentImport ScnUserBlockingManager scnUserBlockingManager,
			@ComponentImport GlobalSettingsManager scnGlobalPermissionManager,
			@ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		this.userManager = ComponentAccessor.getUserManager();
		this.permissionManager = permissionManager;
		this.authenticationContext = authenticationContext;
		this.projectManager = projectManager;
		this.extendedConstantsManager = defaultExtendedConstantsManager;
		this.scnGlobalPermissionManager = scnGlobalPermissionManager;
		this.iWorklogLogtimeManager = new WorklogLogtimeManager(userManager, projectManager, issueManager,
				permissionManager, scnWorklogManager, projectRoleManager, overridedWorklogManager,
				extendedConstantsManager, ofBizScnWorklogStore, projectSettignsManager, scnUserBlockingManager,
				scnDefaultWorklogService);
		this.wicketManager = new WicketManager(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
	}

	@POST
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getTimesheet(@Context HttpServletRequest request, @FormParam("prjList") String prjList,
			@FormParam("usersSelected") String usersSelected, @FormParam("viewType") String viewType) {
		int scnWl = ServletUtil.getIntParam(request, "scnWl", 1);
		int extWl = ServletUtil.getIntParam(request, "extWl", 1);
		int assignedCheck = ServletUtil.getIntParam(request, "assignedCheck", 1);

		// Logged in user
		ApplicationUser user = getUser(request);

		List<String> selectedProjects = (prjList != null && !prjList.equals("")) ? Arrays.asList(prjList.split(","))
				: new ArrayList<String>();
		List<String> selectedUsers = (usersSelected != null && !usersSelected.isEmpty())
				? Arrays.asList(usersSelected.split(",")).stream().map(String::trim).map(String::toLowerCase).distinct()
						.map(userManager::getUserByName).filter(x -> x != null).map(ApplicationUser::getKey).collect(
								Collectors.toList())
				: Arrays.asList(user.getKey());
		VelocityManager vm = ComponentAccessor.getVelocityManager();
		Response response;
		try {
			response = Response
					.ok(new LogTimeRepresentation(vm.getBody("template/", "logtime.vm", getVelocityParams(request,
							selectedProjects, scnWl, extWl, assignedCheck, selectedUsers, viewType))))
					.cacheControl(getNoCacheControl()).build();
		} catch (VelocityException e) {
			LOGGER.error(e.getMessage());
			response = Response.serverError().build();
		}
		return response;
	}

	private ApplicationUser getUser(HttpServletRequest request) {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
	}

	private Map<String, Object> getVelocityParams(HttpServletRequest request, List<String> projectIds, int scnWlCheck,
			int extWlCheck, int assignedCheck, List<String> users, String viewType) {
		int currentPeriod = ServletUtil.getIntParam(request, "currentPeriod", 0);
		int currentslideStep = ServletUtil.getIntParam(request, "currentslideStep", 0);

		ApplicationUser appuser = getUser(request);

		Map<String, Object> params = getVelocityParams(appuser, projectIds, scnWlCheck, extWlCheck, assignedCheck,
				users, viewType, currentPeriod, currentslideStep);

		params.put("i18n", this.authenticationContext.getI18nHelper());
		params.put("req", request);

		return params;
	}

	private Map<String, Object> getVelocityParams(ApplicationUser loggeduser, List<String> projectIds, int scnWlCheck,
			int extWlCheck, int assignedCheck, List<String> users, String viewType, int currentperiod,
			int currentslideStep) {
		Date date = new Date();

		Map<String, Object> params = new HashMap<String, Object>();

		params.put("loggedin", true);
		params.put("loggeduser", loggeduser != null ? loggeduser.getName() : "");
		params.put("loggeduserKey", loggeduser != null ? loggeduser.getKey() : "");

		Date startDate = DateUtils.getMonthStartDate(0, date);
		Date endDate = DateUtils.getMonthEndDate(0, date);
		int slide = currentslideStep;

		if (viewType != null && viewType.equals("Monthly view")) {
			// if period was switched we need to recount slideStep from Monthly
			// to weekly
			if (currentperiod == 1) {
				Date startDateMonth = DateUtils.getMonthStartDate(currentslideStep, date);
				startDate = DateUtils.getWeekStartDate(0, startDateMonth);
				endDate = DateUtils.getWeekEndDate(0, startDateMonth);
				slide = DateUtils.getWeekSlide(date, startDateMonth);
			} else {
				startDate = DateUtils.getWeekStartDate(currentslideStep, date);
				endDate = DateUtils.getWeekEndDate(currentslideStep, date);
			}
		} else {
			if (currentperiod == 1) {
				Date startDateWeek = DateUtils.getWeekEndDate(currentslideStep, date);
				startDate = DateUtils.getMonthStartDate(0, startDateWeek);
				endDate = DateUtils.getMonthEndDate(0, startDateWeek);
				slide = DateUtils.getMonthSlide(date, startDateWeek);
			} else {
				startDate = DateUtils.getMonthStartDate(currentslideStep, date);
				endDate = DateUtils.getMonthEndDate(currentslideStep, date);
			}
		}
		Map<String, Map<String, Integer>> calendarMap = wicketManager.gerUsersCalendar(users, startDate, endDate);
		iWorklogLogtimeManager.setCalendarMap(calendarMap);
		Collection<WorklogType> wlTypes = extendedConstantsManager.getWorklogTypeObjects();
		List<String> datesWeekString = DateUtils.getStringListDate(startDate, endDate);
		boolean scnWlCh = (scnWlCheck == 1 ? true : false);
		boolean extWlCh = (extWlCheck == 1 ? true : false);
		boolean assignedCh = false;

		params.put("slideStep", slide);
		params.put("slidePeriod", DateUtils.formSlidePeriod(startDate, endDate, viewType));

		params.put("wlTypes", wlTypes);
		params.put("wlTypesCombo", getWlTypeRepresentations(wlTypes));
		params.put("weekDaysString", datesWeekString);
		params.put("startDate", startDate.getTime());
		params.put("endDate", endDate.getTime());
		Map<String, WicketRepresentation> userWickets = wicketManager.gerUserWicketTimeForthePeriods(users, startDate,
				endDate);
		params.put("userWickets", userWickets);
		params.put("wicketPermission", wicketManager.gerUserWicketPermission(loggeduser.getKey()));

		boolean hasScnWLPermission = scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, loggeduser);
		if (!hasScnWLPermission) {
			scnWlCh = false;
		}
		params.put("scnWlCheck", scnWlCh);
		params.put("extWlCheck", extWlCh);
		List<LTProjectsRepresentation> ltProjectsRepresentations = getLTProjectRepresentation(loggeduser, projectIds,
				startDate, endDate, scnWlCh, extWlCh, assignedCh, users, calendarMap);
		Collections.sort(ltProjectsRepresentations, new Comparator<LTProjectsRepresentation>() {

			public int compare(LTProjectsRepresentation o1, LTProjectsRepresentation o2) {
				if (o1.getUserName() != null && o2.getUserName() != null) {
					return o1.getUserName().compareTo(o2.getUserName());
				} else {
					return 0;
				}
			}
		});
		params.put("textUtil", new TextFormatUtil());
		params.put("scnWlChShow", hasScnWLPermission);
		params.put("projects", ltProjectsRepresentations);
		params.put("projectUserKeys", ltProjectsRepresentations);
		params.put("projectsTest", getProjectRepresentations(loggeduser));
		return params;
	}

	private List<ProjectRepresentation> getProjectRepresentations(ApplicationUser loggeduser) {
		Collection<Project> projectsTest = permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS,
				loggeduser);
		List<ProjectRepresentation> projectRepresentations = new ArrayList<ProjectRepresentation>();
		ProjectRepresentation pr = new ProjectRepresentation(0L, "", "");
		projectRepresentations.add(pr);
		for (Project project : projectsTest) {
			projectRepresentations.add(new ProjectRepresentation(project));
		}

		return projectRepresentations;
	}

	private List<WLsTypeRepresentation> getWlTypeRepresentations(Collection<WorklogType> wlTypes) {
		List<WLsTypeRepresentation> wLsTypeRepresentations = new ArrayList<WLsTypeRepresentation>();
		WLsTypeRepresentation wLsTypeRepresentation = new WLsTypeRepresentation();
		wLsTypeRepresentation.setWlTypeId("0");
		wLsTypeRepresentation.setWlTypeName("Undefined Type");
		wLsTypeRepresentations.add(wLsTypeRepresentation);
		for (WorklogType wlType : wlTypes) {
			WLsTypeRepresentation wLsTypeRepresentation1 = new WLsTypeRepresentation();
			wLsTypeRepresentation1.setWlTypeId(wlType.getId());
			wLsTypeRepresentation1.setWlTypeName(wlType.getName());
			wLsTypeRepresentations.add(wLsTypeRepresentation1);
		}
		return wLsTypeRepresentations;
	}

	private List<LTProjectsRepresentation> getLTProjectRepresentation(ApplicationUser loggedUser,
			List<String> projectIds, Date startDate, Date endDate, boolean scnWlCheck, boolean extWlCheck,
			boolean assignedCh, List<String> users, Map<String, Map<String, Integer>> calendarMap) {
		List<LTProjectsRepresentation> representations = new ArrayList<LTProjectsRepresentation>();
		List<Long> projectIdsLong = (projectIds.size() != 0)
				? projectIds.stream().filter(x -> x != null && !x.isEmpty()).map(x -> Long.valueOf(x.trim()))
						.collect(Collectors.toList())
				: permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, loggedUser).stream()
						.map(Project::getId).collect(Collectors.toList());
		List<Project> projects = projectManager
				.convertToProjectObjects(Stream.of(
						scnWlCheck
								? iWorklogLogtimeManager.getProjectIdsWithScnWorklogsBetweenDates(projectIdsLong, users,
										DateUtils.getStartDate(-28, startDate), DateUtils.getEndDate(28, endDate))
								: new ArrayList<Long>(),
						extWlCheck
								? iWorklogLogtimeManager.getProjectIdsWithExtWorklogsBetweenDates(projectIdsLong, users,
										DateUtils.getStartDate(-28, startDate), DateUtils.getEndDate(28, endDate))
								: new ArrayList<Long>())
						.flatMap(List::stream).distinct().collect(Collectors.toList()))
				.stream()
				.sorted((o1, o2) -> (o1.getName() != null && o2.getName() != null)
						? o1.getName().compareTo(o2.getName())
						: 0)
				.collect(Collectors.toList());

		for (String userString : users) {
			userString = userString.trim();
			LTProjectsRepresentation ltProjectsRepresentation = new LTProjectsRepresentation();
			Map<String, Integer> userMap = null;
			if (calendarMap != null) {
				userMap = calendarMap.get(userString);
			}

			List<Date> datesWeek = DateUtils.getDatesListDate(startDate, endDate);

			List<WeekRepresentation> weekRepresentations = DateUtils.getWeekRepresentationList(datesWeek, userMap);
			ltProjectsRepresentation.setWeekRepresentations(weekRepresentations);

			List<LTProjectRepresentation> list = new ArrayList<LTProjectRepresentation>();

			if (userString.equals("") || userString.equals(" ")) {
				continue;
			}

			ApplicationUser user = userManager.getUserByKey(userString);

			if (user == null) {
				LOGGER.error("USER WAS NOT FOUND!");
				continue;
			}

			List<String> dates = DateUtils.getDatesList(startDate, endDate);
			Map<String, Integer> totalScnList = new HashMap<String, Integer>();
			Map<String, Integer> totalExtList = new HashMap<String, Integer>();

			for (Project project : projects) {
				LTProjectRepresentation ltProjectRepresentation = iWorklogLogtimeManager
						.getLTProjectRepresentationBetweenDates(loggedUser, project, startDate, endDate, scnWlCheck,
								extWlCheck, assignedCh, userString);

				for (String date : dates) {
					Integer totalScn = totalScnList.get(date) == null ? new Integer(0) : totalScnList.get(date);
					totalScn = totalScn + (ltProjectRepresentation == null ? new Integer(0)
							: ltProjectRepresentation.getScnWlTotal().get(date));
					totalScnList.put(date, totalScn);

					Integer totalExt = totalExtList.get(date) == null ? new Integer(0) : totalExtList.get(date);
					totalExt = totalExt + (ltProjectRepresentation == null ? new Integer(0)
							: ltProjectRepresentation.getExtWlTotal().get(date));
					totalExtList.put(date, totalExt);
				}
				if (ltProjectRepresentation == null)
					continue;

				list.add(ltProjectRepresentation);
			}
			Integer scnProjectsTotal = 0;
			Integer extProjectsTotal = 0;

			List<String> scnTotalList = new ArrayList<String>();
			List<String> extTotalList = new ArrayList<String>();
			for (String date : dates) {
				scnTotalList.add(TextFormatUtil
						.timeToString(String.valueOf(totalScnList.get(date) == null ? "0" : totalScnList.get(date))));
				extTotalList.add(TextFormatUtil
						.timeToString(String.valueOf(totalExtList.get(date) == null ? "0" : totalExtList.get(date))));
				scnProjectsTotal += (totalScnList.get(date) == null) ? 0 : totalScnList.get(date);
				extProjectsTotal += (totalExtList.get(date) == null) ? 0 : totalExtList.get(date);
			}

			ltProjectsRepresentation.setProjects(list);
			ltProjectsRepresentation.setScnWlTotal(scnTotalList);
			ltProjectsRepresentation.setExtWlTotal(extTotalList);

			ltProjectsRepresentation.setScnProjectsTotal(TextFormatUtil.timeToString(String.valueOf(scnProjectsTotal)));
			ltProjectsRepresentation.setExtProjectsTotal(TextFormatUtil.timeToString(String.valueOf(extProjectsTotal)));

			ltProjectsRepresentation.setUserKey(userString);
			ltProjectsRepresentation.setUserName(user.getDisplayName().replace(",", " "));

			representations.add(ltProjectsRepresentation);

		}

		return representations;
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}
