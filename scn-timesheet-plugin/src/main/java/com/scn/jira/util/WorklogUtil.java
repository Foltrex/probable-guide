package com.scn.jira.util;

import java.sql.Timestamp;
import java.util.Date;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;

public class WorklogUtil
{
	public static IScnWorklog convertToWorklog(ProjectRoleManager projectRoleManager, GenericValue gv, Issue issue)
	{
		Timestamp startDateTS = gv.getTimestamp("startdate");
		Timestamp createdTS = gv.getTimestamp("created");
		Timestamp updatedTS = gv.getTimestamp("updated");
		IScnWorklog worklog = new ScnWorklogImpl(
				projectRoleManager,
				issue, 
				gv.getLong("id"),
				gv.getString("author"),
				gv.getString("body"),
				startDateTS != null ? new Date(startDateTS.getTime()) : null,
				gv.getString("grouplevel"),
				gv.getLong("rolelevel"),
				gv.getLong("timeworked"),
				gv.getString("updateauthor"),
				createdTS != null ? new Date(createdTS.getTime()) : null,
				updatedTS != null ? new Date(updatedTS.getTime()) : null,
				gv.getString("worklogtype"));
		return worklog;
	}
}