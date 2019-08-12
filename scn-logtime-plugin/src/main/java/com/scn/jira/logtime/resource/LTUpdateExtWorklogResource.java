package com.scn.jira.logtime.resource;

import java.util.Date;

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

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.logtime.store.ExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/updateExtWorklog")
public class LTUpdateExtWorklogResource {
	private static final Logger LOGGER = Logger.getLogger(LTUpdateExtWorklogResource.class);
	
	private UserManager userManager;
	private PermissionManager permissionManager;
	private JiraAuthenticationContext authenticationContext;
	
	private ProjectManager projectManager;
	private IssueManager issueManager;
	
	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	private ExtendedConstantsManager extendedConstantsManager;
	
	private IScnWorklogManager scnWorklogManager;
	private ExtendedWorklogManager extendedWorklogManager;
	
	private IScnProjectSettingsManager projectSettignsManager;
	private IScnWorklogStore OfBizScnWorklogStore;
	private IScnUserBlockingManager scnUserBlockingManager;
	
	private IExtWorklogLogtimeStore iExtWorklogLogtimeStore;
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
	
	private IScnWorklogService scnDefaultWorklogService;
	
	@Inject
	public LTUpdateExtWorklogResource(PermissionManager permissionManager,
			@ComponentImport JiraAuthenticationContext authenticationContext,
			@ComponentImport ProjectManager projectManager, @ComponentImport IssueManager issueManager,
			@ComponentImport ProjectRoleManager projectRoleManager,
			@Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
			@ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
			@ComponentImport DefaultScnWorklogManager scnWorklogManager,
			@ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
			@ComponentImport ScnProjectSettingsManager projectSettignsManager,
			@ComponentImport OfBizScnWorklogStore ofBizScnWorklogStore,
			@ComponentImport ScnUserBlockingManager scnUserBlockingManager,
			@ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		this.userManager = ComponentAccessor.getUserManager();
		this.permissionManager = permissionManager;
		this.authenticationContext = authenticationContext;
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
		this.iExtWorklogLogtimeStore = new ExtWorklogLogtimeStore(issueManager, worklogManager, extendedWorklogManager);
		this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager, worklogManager,
				projectSettignsManager, scnUserBlockingManager, scnDefaultWorklogService);
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
	public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("complexWLId") String complexWLId,
			@QueryParam("complexId2") String complexId2, @QueryParam("newValue") String newValue, @QueryParam("newWLType") String newWLType,
			@QueryParam("comment") String comment) {
		
		long issueId = getWlIdFromRequestParameter(complexWLId, 0).equals("") ? -1 : Integer.valueOf(getWlIdFromRequestParameter(complexWLId, 0));
		long worklogId = getWlIdFromRequestParameter(complexWLId, 2).equals("") ? -1 : Integer.valueOf(getWlIdFromRequestParameter(complexWLId, 2));
		String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(complexWLId, 1).equals("") ? "" : Integer
				.valueOf(getWlIdFromRequestParameter(complexWLId, 1)));
		String date = getWlIdFromRequestParameter(complexWLId, 3).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 3));
		String userCreated = getWlIdFromRequestParameter(complexWLId, 5).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 5));
		userCreated= userCreated!=null?userCreated.toLowerCase():"";
		
		if(newValue!=null){
			newValue = newValue.trim();
		}
		
		long worklogScnId = 0;
		//this is if we need to update the link of 2 worklogs
		if (complexId2 != null && !complexId2.equals("")) {
			worklogScnId = getWlIdFromRequestParameter(complexId2, 2).equals("") ? 0 : Integer.valueOf(getWlIdFromRequestParameter(complexId2, 2));
		}
		
		comment = (comment != null && !comment.equals("undefined")) ? comment : null;
		String wlType = (newWLType != null && !newWLType.equals("undefined")) ? newWLType : worklogTypeId;
		
		boolean reloadRequired = false;
		
		@SuppressWarnings("unused")
		boolean result = false;
		// Check what to do with the worklog
		Long timeSpent = 0L;
		if(TextFormatUtil.matchesPattern1(newValue)){
			timeSpent = TextFormatUtil.stringToTime(newValue);
		}else{
			if(TextFormatUtil.matchesPattern2(newValue)){
				timeSpent = TextFormatUtil.string2ToTime(newValue);
			}else{
				if(TextFormatUtil.matchesPattern3(newValue)){
					timeSpent = TextFormatUtil.string3ToTime(newValue);
				}else{
					LTMessages message = new LTMessages("DONE EXT!", false, false);
					return Response.ok(message).build();
				}
			}	
			
		}
		
		Issue issue = this.issueManager.getIssueObject(issueId);
		Project prj = issue.getProjectObject();
		if(prj!=null){
			ApplicationUser appuser = getUser(request);
			
			boolean projectPermission = projectSettignsManager.hasPermissionToViewWL(appuser, prj);
			if(!projectPermission){
				LOGGER.info("The user does not have permission to create Ext worklog");
				LTMessages message = new LTMessages("DONE EXT!", false, false);
				return Response.ok(message).build();
			}
		}
	
	
		Date day = DateUtils.stringToDate(date);
		
		boolean isBlocked = iScnWorklogLogtimeStore.isProjectWLWorklogBlocked(prj.getId(), day);
		
		if(isBlocked){
			LTMessages message = new LTMessages("BLOCKED", false, false,null);
			return Response.ok(message).build();
		}
		
		Long wlIdExt = 0L;
		if (worklogId == 0) {
		
			if (day != null) {
				
				if (newValue != null && !newValue.equals("00:00") && !newValue.equals("0") && !newValue.equals("") && !newValue.equals("0h")) {
				//	Long timeSpent = TextFormatUtil.stringToTime(newValue);
					if (timeSpent.longValue() != 0) {
						// Here we will create a worklog
						if (userCreated == null) {
							userCreated = getUser(request).getDisplayName();
						}
						wlIdExt = createExtWorklog(issueId, worklogId, wlType, timeSpent, (comment != null) ? comment : "", userCreated, day, worklogScnId);
						
						if (!wlType.equals(worklogTypeId)) {						
							reloadRequired = true;
						}	
					}
				}
			}
		}
		else {
			GenericValue exWlGeneric = extendedWorklogManager.getExtWorklog(new Long(worklogId));
			if (exWlGeneric != null) {
				if (newValue != null && !newValue.equals("00:00") && !newValue.equals("0") && !newValue.equals("") && !newValue.equals("0h")) {
					//Long timeSpent = TextFormatUtil.stringToTime(newValue);
					updateExtWorklog(worklogId, wlType, timeSpent, comment);
					
					if (!wlType.equals(worklogTypeId)) {
						reloadRequired = true;
					}
					wlIdExt = worklogId;
				}
				else {					
					result = deleteExtWorklog(worklogId);
				}
			}
			
		}
		String complexWLIdnew = changeWlIdFromRequestParameter(complexWLId,  wlIdExt);		
		LTMessages message = new LTMessages("DONE EXT!", false, reloadRequired, complexWLIdnew);

		return Response.ok(message).cacheControl(getNoCacheControl()).build();
	}

    private ApplicationUser getUser(HttpServletRequest request) {
        return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
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
	
	public String changeWlIdFromRequestParameter(String identifier, Long newWLId) {
		// TESS-1_10000_0_08-01_143
		// 2-wlid
		// 3-date
		// 1-wlTypeId
		// 0-issueId
		String res = identifier;
		if (identifier != null && identifier.contains("_")) {
			String[] arr = identifier.split("_");
			res = arr[0]+"_"+arr[1]+"_"+newWLId+"_"+arr[3]+"_"+arr[4]+"_"+arr[5];
		}
		
		return res;
		
	}
	
	private Long createExtWorklog(Long issueId, Long _worklogId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			long worklogScnId) {
		
		Worklog worklog = iExtWorklogLogtimeStore.createExtWorklog(issueId, _worklogType, _timeSpent, _comment, authorKey, date);
		if (worklogScnId != 0) {
			LOGGER.info("Worklogs should be linked as we create an extWorklog we also need to make a ling to worklog with id  "+worklogScnId);
			iScnWorklogLogtimeStore.updateScnWorklogAndExt(worklogScnId, worklog);
			iExtWorklogLogtimeStore.updateExtWorklog(worklog.getId(), _worklogType, _timeSpent, _comment);
		}
		return worklog.getId();
	}
	
	private void updateExtWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) {
		iExtWorklogLogtimeStore.updateExtWorklog(_worklogId, _worklogType, _timeSpent, _comment);
	}
	
	private boolean deleteExtWorklog(Long worklogId) {
		return iExtWorklogLogtimeStore.deleteExtWorklogById(worklogId);
	}
	
	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}
