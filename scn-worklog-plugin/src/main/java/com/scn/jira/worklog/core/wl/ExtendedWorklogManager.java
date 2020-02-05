package com.scn.jira.worklog.core.wl;

import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.worklog.Worklog;

public interface ExtendedWorklogManager {
    Worklog createExtWorklogType(Worklog worklog, String _worklogTypeId) throws DataAccessException;

    List<GenericValue> getExtWorklogsByType(String worklogTypeId);

    void updateExtWorklogType(Long _worklogId, String _worklogType) throws DataAccessException;

    GenericValue getExtWorklog(Long _worklogId) throws DataAccessException;

    boolean deleteExtWorklogType(Long worklogId) throws DataAccessException;
}
