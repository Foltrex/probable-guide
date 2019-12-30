package com.scn.jira.logtime.resource;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.store.ExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
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
import java.util.Date;
import java.util.Objects;

@Named
@Path("/updateExtWorklog")
public class LTUpdateExtWorklogResource {
	private static final Logger LOGGER = Logger.getLogger(LTUpdateExtWorklogResource.class);

	private JiraAuthenticationContext authenticationContext;
	private IssueManager issueManager;
	private WorklogManager worklogManager;
	private IScnProjectSettingsManager projectSettignsManager;
	private IExtWorklogLogtimeStore iExtWorklogLogtimeStore;
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
	private WorklogService worklogService;

	@Inject
	public LTUpdateExtWorklogResource(@ComponentImport JiraAuthenticationContext authenticationContext,
									  @ComponentImport IssueManager issueManager,
									  @ComponentImport ProjectRoleManager projectRoleManager,
									  @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
									  @ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
									  @ComponentImport ScnProjectSettingsManager projectSettignsManager,
									  @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
									  @ComponentImport DefaultScnWorklogService scnDefaultWorklogService,
									  @ComponentImport WorklogService worklogService) {
		this.authenticationContext = authenticationContext;
		this.issueManager = issueManager;
		this.worklogManager = overridedWorklogManager;
		this.projectSettignsManager = projectSettignsManager;
		this.worklogService = worklogService;
		this.iExtWorklogLogtimeStore = new ExtWorklogLogtimeStore(issueManager, overridedWorklogManager, extendedWorklogManager);
		this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager, overridedWorklogManager,
				projectSettignsManager, scnUserBlockingManager, scnDefaultWorklogService);
	}

	@GET
	@AnonymousAllowed
	@Produces({"application/json", "application/xml"})
	public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("complexWLId") String complexWLId,
								 @QueryParam("complexId2") String complexId2, @QueryParam("newValue") String newValue, @QueryParam("newWLType") String newWLType,
								 @QueryParam("comment") String comment) {
		long issueId = getWlIdFromRequestParameter(complexWLId, 0).equals("") ? -1 : Integer.parseInt(getWlIdFromRequestParameter(complexWLId, 0));
		long worklogId = getWlIdFromRequestParameter(complexWLId, 2).equals("") ? -1 : Integer.parseInt(getWlIdFromRequestParameter(complexWLId, 2));
		String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(complexWLId, 1).equals("") ? "" : Integer
				.valueOf(getWlIdFromRequestParameter(complexWLId, 1)));
		String date = getWlIdFromRequestParameter(complexWLId, 3).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 3));
		String userCreated = getWlIdFromRequestParameter(complexWLId, 5).equals("") ? "" : (getWlIdFromRequestParameter(complexWLId, 5));
		userCreated = userCreated != null ? userCreated.toLowerCase() : "";

		if (newValue != null) {
			newValue = newValue.trim();
		}

		long worklogScnId = 0;
		//this is if we need to update the link of 2 worklogs
		if (complexId2 != null && !complexId2.equals("")) {
			worklogScnId = getWlIdFromRequestParameter(complexId2, 2).equals("") ? 0 : Integer.parseInt(getWlIdFromRequestParameter(complexId2, 2));
		}

		comment = (comment != null && !comment.equals("undefined")) ? comment : null;
		String wlType = (newWLType != null && !newWLType.equals("undefined")) ? newWLType : worklogTypeId;

		boolean reloadRequired = false;

		@SuppressWarnings("unused")
		boolean result = false;
		boolean isValueEmplty = (newValue == null || newValue.equals("00:00") || newValue.equals("0") || newValue.equals("") || newValue.equals("0h"));
		// Check what to do with the worklog
		Long timeSpent;
		if (TextFormatUtil.matchesPattern1(newValue)) {
			timeSpent = TextFormatUtil.stringToTime(newValue);
		} else {
			if (TextFormatUtil.matchesPattern2(newValue)) {
				timeSpent = TextFormatUtil.string2ToTime(newValue);
			} else {
				if (TextFormatUtil.matchesPattern3(newValue)) {
					timeSpent = TextFormatUtil.string3ToTime(newValue);
				} else {
					LTMessages message = new LTMessages("DONE EXT!", false, false);
					return Response.ok(message).build();
				}
			}
		}

		Issue issue = this.issueManager.getIssueObject(issueId);
		Project prj = issue.getProjectObject();
		ApplicationUser appUser = getLoggedInUser();
		if (prj != null) {
			boolean projectPermission = projectSettignsManager.hasPermissionToViewWL(appUser, prj);
			if (!projectPermission) {
				LOGGER.info("The user does not have permission to create Ext worklog");
				LTMessages message = new LTMessages("DONE EXT!", false, false);
				return Response.ok(message).build();
			}
		}
		Worklog worklog = worklogManager.getById(worklogId);
		Date day = DateUtils.stringToDate(date);
		boolean isBlocked = iScnWorklogLogtimeStore.isProjectWLWorklogBlocked(Objects.requireNonNull(prj).getId(), day);
		final JiraServiceContext serviceContext = new JiraServiceContextImpl(appUser);
		if (isBlocked || (worklogId != 0 && isValueEmplty && !worklogService.hasPermissionToDelete(serviceContext, worklog))
				|| (worklogId != 0 && !isValueEmplty && !worklogService.hasPermissionToUpdate(serviceContext, worklog))
				|| (worklogId == 0) && !isValueEmplty && !worklogService.hasPermissionToCreate(serviceContext, issue, false)) {
			LTMessages message = new LTMessages("BLOCKED", false, false, null);
			return Response.ok(message).build();
		}

		Long wlIdExt = 0L;
		if (worklogId == 0) {
			if (day != null) {
				if (!isValueEmplty) {
					if (timeSpent != 0) {
						// Here we will create a worklog
						wlIdExt = createExtWorklog(issueId, wlType, timeSpent, (comment != null) ? comment : "", userCreated, day, worklogScnId);
						if (!wlType.equals(worklogTypeId)) {
							reloadRequired = true;
						}
					}
				}
			}
		} else if (worklog != null) {
			if (!isValueEmplty) {
				updateExtWorklog(worklogId, wlType, timeSpent, comment);
				if (!wlType.equals(worklogTypeId)) {
					reloadRequired = true;
				}
				wlIdExt = worklogId;
			} else {
				deleteExtWorklog(worklogId);
			}
		}
		String complexWLIdnew = changeWlIdFromRequestParameter(complexWLId, wlIdExt);
		LTMessages message = new LTMessages("DONE EXT!", false, reloadRequired, complexWLIdnew);

		return Response.ok(message).cacheControl(getNoCacheControl()).build();
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
		} else {
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
			res = arr[0] + "_" + arr[1] + "_" + newWLId + "_" + arr[3] + "_" + arr[4] + "_" + arr[5];
		}

		return res;
	}

	private ApplicationUser getLoggedInUser() {
		return authenticationContext.getLoggedInUser();
	}

	private Long createExtWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
								  long worklogScnId) {
		Worklog worklog = iExtWorklogLogtimeStore.createExtWorklog(issueId, _worklogType, _timeSpent, _comment, authorKey, date);
		if (worklogScnId != 0) {
			LOGGER.info("Worklogs should be linked as we create an extWorklog we also need to make a ling to worklog with id  " + worklogScnId);
			iScnWorklogLogtimeStore.updateScnWorklogAndExt(worklogScnId, worklog);
			iExtWorklogLogtimeStore.updateExtWorklog(worklog.getId(), _worklogType, _timeSpent, _comment);
		}
		return worklog.getId();
	}

	private void updateExtWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) {
		iExtWorklogLogtimeStore.updateExtWorklog(_worklogId, _worklogType, _timeSpent, _comment);
	}

	private void deleteExtWorklog(Long worklogId) {
		iExtWorklogLogtimeStore.deleteExtWorklogById(worklogId);
	}

	@Nonnull
	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}
}
