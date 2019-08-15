package com.scn.jira.logtime.resource;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/updateScnWorklogs")
public class LTUpdateScnWorklogsResource {
	private IssueManager issueManager;
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;

	@Inject
	public LTUpdateScnWorklogsResource(@ComponentImport IssueManager issueManager,
			@ComponentImport ProjectRoleManager projectRoleManager,
			@Qualifier("overridedWorklogManager") WorklogManager overridedWorklogManager,
			@ComponentImport ScnProjectSettingsManager projectSettignsManager,
			@ComponentImport ScnUserBlockingManager scnUserBlockingManager,
			@ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		this.issueManager = issueManager;
		this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(issueManager, projectRoleManager,
				overridedWorklogManager, projectSettignsManager, scnUserBlockingManager, scnDefaultWorklogService);
	}

	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getWorklogForUpdate(@Context HttpServletRequest request,
			@QueryParam("wlsToSave") List<String> wlsToSave, @QueryParam("issueId") String issueId) {
		if (wlsToSave == null)
			return Response.ok("NOTHING TO SAVE").build();

		Issue issue = issueManager.getIssueObject(Long.parseLong(issueId));
		Map<String, String> wlToCreate = wlsToSave.stream()
				.collect(Collectors.toMap(v -> getWlIdFromRequestParameter(v, 2), v -> v, (p, n) -> n)).entrySet()
				.stream().filter(e -> !getWlIdFromRequestParameter(e.getValue(), 0).isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (String wlKey : wlToCreate.keySet()) {
			String wlString = wlToCreate.get(wlKey);
			String time = getWlIdFromRequestParameter(wlString, 0);
			String comment = String.valueOf(getWlIdFromRequestParameter(wlString, 1));
			String date = getWlIdFromRequestParameter(wlString, 2);
			String userKey = getWlIdFromRequestParameter(wlString, 3).equals("") ? null
					: getWlIdFromRequestParameter(wlString, 3);

			String worklogTypeId = String.valueOf(getWlIdFromRequestParameter(wlString, 4).equals("") ? ""
					: Integer.valueOf(getWlIdFromRequestParameter(wlString, 4)));

			userKey = userKey != null ? userKey.toLowerCase() : "";

			createWorklogS(time, comment, date, userKey, worklogTypeId, issueId);
		}

		LTMessages message = new LTMessages("DONE!");
		message.setMessage(issue != null ? String.valueOf(issue.getProjectObject().getId()) : "");
		return Response.ok(message).build();
	}

	private void createWorklogS(String time, String comment, String date, String userKey, String worklogTypeId,
			String issueId) {
		Long timeSpent = 0L;
		if (TextFormatUtil.matchesPattern1(time)) {
			timeSpent = TextFormatUtil.stringToTime(time);
		} else {
			if (TextFormatUtil.matchesPattern2(time)) {
				timeSpent = TextFormatUtil.string2ToTime(time);
			} else {
				if (TextFormatUtil.matchesPattern3(time)) {
					timeSpent = TextFormatUtil.string3ToTime(time);
				}
			}
		}
		Date day = DateUtils.stringToDate(date);
		if (day != null)
			if (time != null && !time.equals("00:00") && !time.equals("0") && !time.equals(""))
				// Long timeSpent = TextFormatUtil.stringToTime(time);
				if (timeSpent.longValue() != 0)
					// Here we will create a worklog
					if (issueId != null && worklogTypeId != null)
						createScnWorklog(Long.parseLong(issueId), null, worklogTypeId, timeSpent,
								comment != null ? comment : "", userKey, day, String.valueOf(worklogTypeId));
	}

	private boolean createScnWorklog(Long issueId, Long _worklogId, String _worklogType, Long _timeSpent,
			String _comment, String authorKey, Date date, String worklogTypeId) {
		return iScnWorklogLogtimeStore.createScnWorklog(issueId, _worklogType, _timeSpent, _comment, authorKey, date,
				worklogTypeId);
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
}
