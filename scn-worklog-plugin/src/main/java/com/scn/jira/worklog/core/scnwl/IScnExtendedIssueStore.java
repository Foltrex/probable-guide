package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 19.08.2010
 * Time: 14:49:08
 * To change this template use File | Settings | File Templates.
 */
public interface IScnExtendedIssueStore {
    public abstract IScnExtendedIssue create(IScnExtendedIssue extIssue) throws DataAccessException;

    public abstract IScnExtendedIssue update(IScnExtendedIssue extIssue) throws DataAccessException;

    public abstract boolean delete(Long id) throws DataAccessException;

    public abstract IScnExtendedIssue getByIssue(Issue issue) throws DataAccessException;
}
