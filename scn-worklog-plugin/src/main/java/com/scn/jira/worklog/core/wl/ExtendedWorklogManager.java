package com.scn.jira.worklog.core.wl;

import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.worklog.Worklog;

public interface ExtendedWorklogManager
{
	public Worklog createExtWorklogType(Worklog worklog, String _worklogTypeId) throws DataAccessException;
	
	public List<GenericValue> getExtWorklogsByType(String worklogTypeId);

	public void updateExtWorklogType(Long _worklogId, String _worklogType) throws DataAccessException;

	public GenericValue getExtWorklog(Long _worklogId) throws DataAccessException;
	
	public boolean deleteExtWorklogType(Long worklogId) throws DataAccessException;
}