package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;

public interface IScnExtendedIssueStore {
	public abstract IScnExtendedIssue create(IScnExtendedIssue extIssue) throws DataAccessException;

	public abstract IScnExtendedIssue update(IScnExtendedIssue extIssue) throws DataAccessException;

	public abstract boolean delete(Long id) throws DataAccessException;

	public abstract IScnExtendedIssue getByIssue(Issue issue) throws DataAccessException;
}
