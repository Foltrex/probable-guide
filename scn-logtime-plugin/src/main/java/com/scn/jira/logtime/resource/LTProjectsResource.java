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
import java.util.Locale;
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
import org.apache.velocity.exception.VelocityException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.velocity.VelocityManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.logtime.representation.ProjectRepresentation;
import com.scn.jira.logtime.representation.WLsTypeRepresentation;
import com.scn.jira.logtime.representation.WeekRepresentation;
import com.scn.jira.logtime.representation.WicketRepresentation;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/projectsobj")
public class LTProjectsResource {
	private UserManager userManager;
	private PermissionManager permissionManager;
	private JiraAuthenticationContext authenticationContext;
	private final OutlookDateManager outlookDateManager;

	private ProjectManager projectManager;
	private ExtendedConstantsManager extendedConstantsManager;

	private I18nResolver i18nResolver;
	private IGlobalSettingsManager scnGlobalPermissionManager;

	private IWorklogLogtimeManager iWorklogLogtimeManager;
	private WicketManager wicketManager;

	/**
	 * Constructor.
	 * 
//	 * @param userManager
	 *            a SAL object used to find remote usernames in Atlassian products
	 * @param userUtil
	 *            a JIRA object to resolve usernames to JIRA's internal {@code com.opensymphony.os.User} objects
	 * @param permissionManager
	 *            the JIRA object which manages permissions for users and projects
	 */

	@Inject
	public LTProjectsResource(@ComponentImport PermissionManager permissionManager,
			  @ComponentImport UserUtil userUtil, @ComponentImport JiraAuthenticationContext authenticationContext,
			  @ComponentImport OutlookDateManager outlookDateManager, @ComponentImport ProjectManager projectManager,
			  @ComponentImport IssueManager issueManager, @ComponentImport ProjectRoleManager projectRoleManager,
			  WorklogManager overridedWorklogManager,
			  @ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
			  @ComponentImport DefaultScnWorklogManager scnWorklogManager,
			  @ComponentImport OfBizScnWorklogStore ofBizScnWorklogStore,
			  @ComponentImport ScnProjectSettingsManager projectSettignsManager,
			  @ComponentImport I18nResolver i18nResolver, @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
			  @ComponentImport GlobalSettingsManager scnGlobalPermissionManager,
			  @ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		super();
		this.userManager = ComponentAccessor.getUserManager();
		this.permissionManager = permissionManager;
		this.authenticationContext = authenticationContext;
		this.outlookDateManager = outlookDateManager;
		this.projectManager = projectManager;
		this.extendedConstantsManager = defaultExtendedConstantsManager;
		this.i18nResolver = i18nResolver;
		this.scnGlobalPermissionManager = scnGlobalPermissionManager;
		this.iWorklogLogtimeManager = new WorklogLogtimeManager(userManager, projectManager, issueManager, userUtil,
				permissionManager, scnWorklogManager, projectRoleManager, overridedWorklogManager, extendedConstantsManager,
				ofBizScnWorklogStore, projectSettignsManager, scnUserBlockingManager, scnDefaultWorklogService);
		System.out.println("LTProjectsResource CONSTRUCTOR");
		this.wicketManager = new WicketManager(this.i18nResolver);

	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}

	/**
	 * Returns the list of projects browsable by the user in the specified request.
	 * 
	 * @param request
	 *            the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("prjList") String prjList,
			@QueryParam("usersSelected") String usersSelected, @QueryParam("viewType") String viewType) {

		int scnWl = ServletUtil.getIntParam(request, "scnWl", 1);
		int extWl = ServletUtil.getIntParam(request, "extWl", 1);
		int assignedCheck = ServletUtil.getIntParam(request, "assignedCheck", 1);

		// Logged in user
		ApplicationUser user = getUser(request);

		List<String> usersAll = new ArrayList<String>();
		usersAll.add(user.getDisplayName());
		List<String> selectedProjects = (prjList != null && !prjList.equals("")) ? Arrays.asList(prjList.split(","))
				: new ArrayList<String>();

		List<String> selectedUsers = (usersSelected != null && usersSelected.length() != 0 && usersSelected != "") ? Arrays
				.asList(usersSelected.split(",")) : usersAll;

		List<String> usersAllWithouDuplicatesLower = new ArrayList<String>();
		List<String> usersAllWithouDuplicates = new ArrayList<String>();

		for (String userCur : selectedUsers) {
			userCur = userCur.trim();
			if (!usersAllWithouDuplicatesLower.contains(userCur.toLowerCase())) {
				usersAllWithouDuplicatesLower.add(userCur.toLowerCase());
				usersAllWithouDuplicates.add(userCur);
			} else {
				System.out.println("DUPLICATE:" + userCur);
			}
		}

		selectedUsers = usersAllWithouDuplicates;
		VelocityManager vm = ComponentAccessor.getVelocityManager();
		try {
			return Response
					.ok(new LogTimeRepresentation(vm.getBody("template/", "logtime.vm",
							getVelocityParams(request, selectedProjects, scnWl, extWl, assignedCheck, selectedUsers, viewType))))
					.cacheControl(getNoCacheControl()).build();
		} catch (VelocityException e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

    private ApplicationUser getUser(HttpServletRequest request) {
        return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    }

	private Map<String, Object> getVelocityParams(HttpServletRequest request, List<String> projectIds, int scnWlCheck,
			int extWlCheck, int assignedCheck, List<String> users, String viewType) {
		int currentPeriod = ServletUtil.getIntParam(request, "currentPeriod", 0);
		int currentslideStep = ServletUtil.getIntParam(request, "currentslideStep", 0);

		ApplicationUser appuser = getUser(request);

		Map<String, Object> params = getVelocityParams(appuser, projectIds, scnWlCheck, extWlCheck, assignedCheck, users,
				viewType, currentPeriod, currentslideStep);

		params.put("i18n", this.authenticationContext.getI18nHelper());
		params.put("req", request);
		return params;
	}

	private Map<String, Object> getVelocityParams(ApplicationUser loggeduser, List<String> projectIds, int scnWlCheck,
			int extWlCheck, int assignedCheck, List<String> users, String viewType, int currentperiod, int currentslideStep) {
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
				System.out.println("SWITCHING THE VIEW TO WEEKLY");
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
				System.out.println("SWITCHING THE VIEW TO MONTHLY");
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

		Map<String, WicketRepresentation> userWickets = wicketManager.gerUserWicketTimeForthePeriods(users, startDate, endDate);

		params.put("userWickets", userWickets);

		params.put("wicketPermission", wicketManager.gerUserWicketPermission(loggeduser.getKey()));

		boolean hasScnWLPermission = scnGlobalPermissionManager.hasPermission(SCN_TIMETRACKING, loggeduser);
		if (!hasScnWLPermission) {
			scnWlCh = false;
		}
		params.put("scnWlCheck", scnWlCh);
		params.put("extWlCheck", extWlCh);

		List<LTProjectsRepresentation> ltProjectsRepresentations = getLTProjectRepresentation(loggeduser, projectIds, startDate,
				endDate, scnWlCh, extWlCh, assignedCh, users, calendarMap);

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

		params.put("outlookDate", this.outlookDateManager.getOutlookDate(Locale.ENGLISH));
		params.put("projectsTest", getProjectRepresentations(loggeduser));

		return params;
	}

	private List<ProjectRepresentation> getProjectRepresentations(ApplicationUser loggeduser) {
		Collection<Project> projectsTest = permissionManager.getProjects(Permissions.BROWSE, loggeduser);

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

	private List<LTProjectsRepresentation> getLTProjectRepresentation(ApplicationUser loggedUser, List<String> projectIds,
			Date startDate, Date endDate, boolean scnWlCheck, boolean extWlCheck, boolean assignedCh, List<String> users,
			Map<String, Map<String, Integer>> calendarMap) {

		List<LTProjectsRepresentation> representations = new ArrayList<LTProjectsRepresentation>();

		List<Project> projects = new ArrayList<Project>();
		if (projectIds.size() != 0) {
			for (String projectId : projectIds) {
				if (projectId != null && !projectId.equals("")) {
					Project prj = this.projectManager.getProjectObj(Long.valueOf(projectId.trim()));
					if (prj != null) {
						projects.add(prj);
					}
				}
			}
		} else {
			Collection<Project> projectsColl = permissionManager.getProjects(Permissions.BROWSE, loggedUser);
			projects.addAll(projectsColl);
		}

		Collections.sort(projects, new Comparator<Project>() {

			public int compare(Project o1, Project o2) {
				if (o1.getName() != null && o2.getName() != null) {
					return o1.getName().compareTo(o2.getName());
				} else {
					return 0;
				}
			}
		});

		System.out.println("USERS selected size list " + users.size());

		for (String userString : users) {
			userString = userString.trim();
			LTProjectsRepresentation ltProjectsRepresentation = new LTProjectsRepresentation();
			Map<String, Integer> userMap = null;
			if (calendarMap != null) {
				userMap = calendarMap.get(userString);
			}

			List<java.util.Date> datesWeek = DateUtils.getDatesListDate(startDate, endDate);

			List<WeekRepresentation> weekRepresentations = DateUtils.getWeekRepresentationList(datesWeek, userMap);
			ltProjectsRepresentation.setWeekRepresentations(weekRepresentations);

			List<LTProjectRepresentation> list = new ArrayList<LTProjectRepresentation>();

			if (userString.equals("") || userString.equals(" ")) {
				continue;
			}

			ApplicationUser user = ComponentAccessor.getUserManager().getUser(userString);

			if (user == null) {
				System.out.println("USER WAS NOT FOUND! ");
				continue;
			}

			List<String> dates = DateUtils.getDatesList(startDate, endDate);
			Map<String, Integer> totalScnList = new HashMap<String, Integer>();
			Map<String, Integer> totalExtList = new HashMap<String, Integer>();

			for (Project project : projects) {

				LTProjectRepresentation ltProjectRepresentation = iWorklogLogtimeManager.getLTProjectRepresentationBetweenDates(
						loggedUser, project, startDate, endDate, scnWlCheck, extWlCheck, assignedCh, userString);

				for (String date : dates) {
					Integer totalScn = totalScnList.get(date) == null ? new Integer(0) : totalScnList.get(date);
					totalScn = totalScn
							+ (ltProjectRepresentation == null ? new Integer(0) : ltProjectRepresentation.getScnWlTotal().get(
									date));
					totalScnList.put(date, totalScn);

					Integer totalExt = totalExtList.get(date) == null ? new Integer(0) : totalExtList.get(date);
					totalExt = totalExt
							+ (ltProjectRepresentation == null ? new Integer(0) : ltProjectRepresentation.getExtWlTotal().get(
									date));
					totalExtList.put(date, totalExt);
				}
				if (ltProjectRepresentation == null) continue;

				list.add(ltProjectRepresentation);
			}
			Integer scnProjectsTotal = 0;
			Integer extProjectsTotal = 0;

			List<String> scnTotalList = new ArrayList<String>();
			List<String> extTotalList = new ArrayList<String>();
			for (String date : dates) {
				scnTotalList.add(TextFormatUtil.timeToString(String.valueOf(totalScnList.get(date) == null ? "0" : totalScnList
						.get(date))));
				extTotalList.add(TextFormatUtil.timeToString(String.valueOf(totalExtList.get(date) == null ? "0" : totalExtList
						.get(date))));
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

}
