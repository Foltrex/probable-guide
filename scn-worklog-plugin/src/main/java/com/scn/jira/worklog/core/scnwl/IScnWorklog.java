package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.issue.worklog.Worklog;

public interface IScnWorklog extends Worklog
{
	String getWorklogTypeId();
	
	void setWorklogTypeId(String worklogType);
	
	Worklog getLinkedWorklog();
	
	void setLinkedWorklog(Worklog linkedWorklog);
}
