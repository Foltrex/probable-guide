package com.scn.jira.logtime.resource;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.logtime.store.ExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.*;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import com.scn.jira.worklog.scnwl.IScnWorklogService;

import org.springframework.beans.factory.annotation.Qualifier;

@Named
@Path("/updateScnWorklog")
public class LTUpdateScnWorklogResource {
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
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
	private IScnProjectSettingsManager projectSettignsManager;
	private IScnWorklogStore OfBizScnWorklogStore;
	private IScnUserBlockingManager scnUserBlockingManager;
	private IExtWorklogLogtimeStore iExtWorklogLogtimeStore;
	private IScnWorklogService scnWorklogService;

	@Inject
	public LTUpdateScnWorklogResource(@ComponentImport PermissionManager permissionManager,
									  @ComponentImport JiraAuthenticationContext authenticationContext,
									  @ComponentImport ProjectManager projectManager, @ComponentImport IssueManager issueManager,
									  @ComponentImport ProjectRoleManager projectRoleManager,
									  @Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
									  @ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
									  @ComponentImport DefaultScnWorklogManager scnWorklogManager,
									  @ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
									  @ComponentImport OfBizScnWorklogStore ofBizScnWorklogStore,
									  @ComponentImport ScnProjectSettingsManager projectSettignsManager,
									  @ComponentImport ScnUserBlockingManager scnUserBlockingManager,
									  @ComponentImport DefaultScnWorklogService scnWorklogService) {
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
		this.scnWorklogService = scnWorklogService;
		this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager,
				worklogManager, projectSettignsManager, scnUserBlockingManager, scnWorklogService);
		this.iExtWorklogLogtimeStore = new ExtWorklogLogtimeStore(issueManager, worklogManager, extendedWorklogManager);
	}

	/**
	 * Returns the list of projects browsable by the user in the specified
	 * request.
	 *
	 * @param request the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({"application/json", "application/xml"})
	public Response getWorklogForUpdate(@Context HttpServletRequest request, @QueryParam("complexWLId") String complexWLId,
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

		comment = (comment != null && !comment.equals("undefined")) ? comment : null;
		String wlType = (newWLType != null && !newWLType.equals("undefined")) ? newWLType : worklogTypeId;

		boolean reloadRequired = false;
		boolean result = false;
		boolean isValueEmplty = (newValue == null || newValue.equals("00:00") || newValue.equals("0") || newValue.equals("") || newValue.equals("0h"));
		// Check what to do with the worklog
		Long timeSpent = 0L;
		if (TextFormatUtil.matchesPattern1(newValue)) {
			timeSpent = TextFormatUtil.stringToTime(newValue);
		} else {
			if (TextFormatUtil.matchesPattern2(newValue)) {
				timeSpent = TextFormatUtil.string2ToTime(newValue);
			} else {
				if (TextFormatUtil.matchesPattern3(newValue)) {
					timeSpent = TextFormatUtil.string3ToTime(newValue);
				}
			}
		}
		Long wlId = 0L;
		Long wlIdExt = 0L;

		Issue issue = this.issueManager.getIssueObject(issueId);
		Date day = DateUtils.stringToDate(date);

		IScnWorklog scnWorklog = scnWorklogManager.getById(worklogId);
		ApplicationUser appUser = getLoggedInUser();
		boolean isBlocked = (iScnWorklogLogtimeStore.isProjectWLBlocked(Objects.requireNonNull(issue.getProjectObject()).getId(), day)
				|| (iScnWorklogLogtimeStore.isWlAutoCopy(issue, wlType)
				&& iScnWorklogLogtimeStore.isProjectWLWorklogBlocked(issue.getProjectObject().getId(), day)));
		boolean hasPermissionToDelete = !isValueEmplty || (scnWorklog != null && scnWorklogService.hasPermissionToDelete(new JiraServiceContextImpl(appUser), scnWorklog));
		if (isBlocked || !hasPermissionToDelete) {
			LTMessages message = new LTMessages("BLOCKED", false, false, null);
			return Response.ok(message).build();
		}

		if (worklogId == 0) {
			if (day != null) {
				if (!isValueEmplty) {
					if (timeSpent != 0) {
						Map<String, Object> resultMap = iScnWorklogLogtimeStore.createScnWorklogResultMap(issueId,
								wlType, timeSpent, comment != null ? comment : "", userCreated, day, wlType);
						result = (Boolean) resultMap.get("isAuto");
						wlId = (Long) resultMap.get("wlId");
						wlIdExt = (Long) resultMap.get("wlIdExt");
						if (!wlType.equals(worklogTypeId)) {
							reloadRequired = true;
						}
					}
				}
			}
		} else if (scnWorklog != null) {
			if (isValueEmplty) {
				result = deleteScnWorklog(worklogId);
			} else {
				result = updateScnWorklog(worklogId, wlType, timeSpent, comment);
				if (!wlType.equals(worklogTypeId)) {
					reloadRequired = true;
				}
				wlId = scnWorklog.getId();
				Worklog ext = scnWorklog.getLinkedWorklog();
				if (ext != null) {
					wlIdExt = ext.getId();
				}
			}
		}

		String complexWLIdnew = changeWlIdFromRequestParameter(complexWLId, wlId),
				complexWLIdExtNew = changeWlIdFromRequestParameter(complexId2, wlIdExt);

		LTMessages message = new LTMessages("DONE SCN!", result, reloadRequired, complexWLIdnew, complexWLIdExtNew);

		return Response.ok(message).build();
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

	private ApplicationUser getLoggedInUser() {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
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

	private boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) {
		return iScnWorklogLogtimeStore.updateScnWorklog(_worklogId, _worklogType, _timeSpent, _comment);
	}

	private boolean deleteScnWorklog(Long worklogId) {
		return iScnWorklogLogtimeStore.deleteScnWorklogById(worklogId);
	}
}
