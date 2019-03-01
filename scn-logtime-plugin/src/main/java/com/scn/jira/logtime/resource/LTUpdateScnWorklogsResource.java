package com.scn.jira.logtime.resource;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/updateScnWorklogs")
public class LTUpdateScnWorklogsResource {
	private UserManager userManager;
	private PermissionManager permissionManager;
	private UserUtil userUtil;
	private JiraAuthenticationContext authenticationContext;
	private final OutlookDateManager outlookDateManager;
	
	private ProjectManager projectManager;
	private IssueManager issueManager;
	
	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	private ExtendedConstantsManager extendedConstantsManager;
	
	private IScnWorklogManager scnWorklogManager;
	private ExtendedWorklogManager extendedWorklogManager;
	
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
	private IScnProjectSettingsManager projectSettignsManager;
	private IScnWorklogStore OfBizScnWorklogStore;
	private IScnUserBlockingManager scnUserBlockingManager;	
	
	private IScnWorklogService scnDefaultWorklogService;
	
	/**
	 * Constructor.
	 * 
//	 * @param userManager
	 *            a SAL object used to find remote usernames in Atlassian
	 *            products
	 * @param userUtil
	 *            a JIRA object to resolve usernames to JIRA's internal
	 *            {@code com.opensymphony.os.User} objects
	 * @param permissionManager
	 *            the JIRA object which manages permissions for users and
	 *            projects
	 */

	@Inject
	public LTUpdateScnWorklogsResource(@ComponentImport PermissionManager permissionManager,
               @ComponentImport UserUtil userUtil, @ComponentImport JiraAuthenticationContext authenticationContext,
               @ComponentImport OutlookDateManager outlookDateManager, @ComponentImport ProjectManager projectManager,
               @ComponentImport IssueManager issueManager, @ComponentImport ProjectRoleManager projectRoleManager,
									   @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
               @ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
               @ComponentImport DefaultScnWorklogManager scnWorklogManager,
               @ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
               @ComponentImport OfBizScnWorklogStore ofBizScnWorklogStore,
               @ComponentImport ScnProjectSettingsManager projectSettignsManager,
               @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
               @ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		super();
		this.userManager = ComponentAccessor.getUserManager();
		this.permissionManager = permissionManager;
		this.userUtil = userUtil;
		this.authenticationContext = authenticationContext;
		this.outlookDateManager = outlookDateManager;
		this.projectManager = projectManager;
		this.issueManager = issueManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = overridedWorklogManager;
		this.extendedConstantsManager = defaultExtendedConstantsManager;
		this.scnWorklogManager = scnWorklogManager;
		this.extendedWorklogManager = extendedWorklogManager;
		this.projectSettignsManager = projectSettignsManager;
		this.OfBizScnWorklogStore = ofBizScnWorklogStore;
		this.scnUserBlockingManager = scnUserBlockingManager;
		this.scnDefaultWorklogService = scnDefaultWorklogService;
		iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(userManager, projectManager, issueManager, permissionManager, projectRoleManager,
				overridedWorklogManager, extendedConstantsManager, OfBizScnWorklogStore, projectSettignsManager, scnUserBlockingManager,scnDefaultWorklogService);
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
	public Response getWorklogForUpdate(@Context HttpServletRequest request, @QueryParam("wlsToSave") List<String> wlsToSave,
			@QueryParam("issueId") String issueId) {
		
		// 07:33_sss_04-10-2013_admin_testing
		if (wlsToSave == null) {
			System.out.println("There is nothing to save");
			return Response.ok("NOTHING TO SAVE").build();
		}
		Issue issue = issueManager.getIssueObject(Long.parseLong(issueId));
		
		Map<String, String> wlToCreate = new HashMap<String, String>();
		
		for (String wl : wlsToSave) {
			String date = getWlIdFromRequestParameter(wl, 2).equals("") ? "" : (getWlIdFromRequestParameter(wl, 2));
			wlToCreate.put(date, wl);
		}
		
		for (String wlKey : wlToCreate.keySet()) {
			
			String wlString = wlToCreate.get(wlKey);
			String time = getWlIdFromRequestParameter(wlString, 0).equals("") ? null : getWlIdFromRequestParameter(wlString, 0);
			String comment = String.valueOf(getWlIdFromRequestParameter(wlString, 1).equals("") ? "" : getWlIdFromRequestParameter(wlString, 1));
			String date = getWlIdFromRequestParameter(wlString, 2).equals("") ? "" : (getWlIdFromRequestParameter(wlString, 2));
			String userKey = getWlIdFromRequestParameter(wlString, 3).equals("") ? null : getWlIdFromRequestParameter(wlString, 3);
			
			String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(wlString, 4).equals("") ? "" : Integer
					.valueOf(getWlIdFromRequestParameter(wlString, 4)));
			
			userKey= userKey!=null?userKey.toLowerCase():"";
			System.out.println("Scn time: " + time + " Scn userKey: " + userKey + " comment: " + comment + " Scn date: " + date
					+ " Scn worklogTypeId: " + worklogTypeId);
			
			createWorklogS(time, comment, date, userKey, worklogTypeId, issueId);
		}
		System.out.println("The worklogs were saved successfully");
		
		LTMessages message = new LTMessages("DONE!");
		message.setMessage(issue != null ? String.valueOf(issue.getProjectObject().getId()) : "");
		return Response.ok(message).build();
	}
	
	public void createWorklogS(String time, String comment, String date, String userKey, String worklogTypeId, String issueId) {
		Long timeSpent = 0L;
		if(TextFormatUtil.matchesPattern1(time)){
			timeSpent = TextFormatUtil.stringToTime(time);
		}else{
			if(TextFormatUtil.matchesPattern2(time)){
				timeSpent = TextFormatUtil.string2ToTime(time);
			}else{
				if(TextFormatUtil.matchesPattern3(time)){
					timeSpent = TextFormatUtil.string3ToTime(time);
				}
			}
		}	
		Date day = DateUtils.stringToDate(date);
		if (day != null) {
			if (time != null && !time.equals("00:00") && !time.equals("0") && !time.equals("")) {
				//Long timeSpent = TextFormatUtil.stringToTime(time);
				if (timeSpent.longValue() != 0) {
					// Here we will create a worklog
					ApplicationUser user = ComponentAccessor.getUserManager().getUser(userKey);
					if (issueId != null && worklogTypeId != null) {
						createScnWorklog(Long.parseLong(issueId), null, worklogTypeId, timeSpent, comment != null ? comment : "", userKey, day,
								String.valueOf(worklogTypeId));
					}
					
					System.out.println("The worklog was created!!");
				}
			}
		}
	}
	
	private boolean createScnWorklog(Long issueId, Long _worklogId, String _worklogType, Long _timeSpent, String _comment, String authorKey,
			Date date, String worklogTypeId) {
		return iScnWorklogLogtimeStore.createScnWorklog(issueId, _worklogType, _timeSpent, _comment, authorKey, date, worklogTypeId);
		
	}
	
	public String getWlIdFromRequestParameter(String identifier, int i) {
		// TESS-1_10000_0_08-01_143
		// 2-wlid
		// 3-date
		// 1-wlTypeId
		// 0-issueId
		if (identifier != null && identifier.contains("_")) {
			String[] arr = identifier.split("_");
			return arr[i];
		}
		else {
			return "";
		}
		
	}
	
}
