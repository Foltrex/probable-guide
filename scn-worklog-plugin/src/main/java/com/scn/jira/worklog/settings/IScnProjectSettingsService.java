package com.scn.jira.worklog.settings;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.scn.jira.worklog.core.wl.WorklogType;

import java.util.Collection;
import java.util.Date;

public interface IScnProjectSettingsService {
    boolean isWLAutoCopyEnabled(JiraServiceContext srvContext, Long projectId);

    void setWLAutoCopy(JiraServiceContext srvContext, Long projectId, boolean value);

    Collection<WorklogType> getWorklogTypes(JiraServiceContext srvContext, Long projectId);

    void setWorklogTypes(JiraServiceContext srvContext, Long projectId, Collection<WorklogType> worklogTypes);

    Collection<WorklogType> getExcludedWorklogTypes(JiraServiceContext srvContext, Long projectId);

    void setExcludedWorklogTypes(JiraServiceContext srvContext, Long projectId, Collection<WorklogType> worklogTypes);

    boolean getUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId);

    void setUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId, boolean value);

    boolean isWLTypeRequired(JiraServiceContext srvContext, Long projectId);

    void setWLTypeRequired(JiraServiceContext srvContext, Long projectId, boolean value);

    WorklogType getDefaultWorklogType(JiraServiceContext srvContext, Long projectId);

    void setDefaultWorklogType(JiraServiceContext srvContext, Long projectId, String worklogTypeId);

    Date getWLBlockingDate(JiraServiceContext srvContext, Long projectId);

    void setWLBlockingDate(JiraServiceContext srvContext, Long projectId, Date value);

    Date getWLWorklogBlockingDate(JiraServiceContext srvContext, Long projectId);

    void setWLWorklogBlockingDate(JiraServiceContext srvContext, Long projectId, Date value);

    Collection<ProjectRole> getProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId);

    void setProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId, Collection<ProjectRole> projectRoles);
}
