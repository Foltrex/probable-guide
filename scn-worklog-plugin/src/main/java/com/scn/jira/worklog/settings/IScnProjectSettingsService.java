package com.scn.jira.worklog.settings;

import java.util.Collection;
import java.util.Date;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.scn.jira.worklog.core.wl.WorklogType;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 05.08.2010
 * Time: 14:05:32
 * To change this template use File | Settings | File Templates.
 */
public interface IScnProjectSettingsService
{
	boolean isWLAutoCopyEnabled(JiraServiceContext srvContext, Long projectId);
	
	void setWLAutoCopy(JiraServiceContext srvContext, Long projectId, boolean value);
	
	Collection<WorklogType> getWorklogTypes(JiraServiceContext srvContext, Long projectId);
	
	void setWorklogTypes(JiraServiceContext srvContext, Long projectId, Collection<WorklogType> worklogTypes);
	
	boolean getUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId);
	
	void setUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId, boolean value);
	
    boolean isWLTypeRequired(JiraServiceContext srvContext, Long projectId);
    
    void setWLTypeRequired(JiraServiceContext srvContext, Long projectId, boolean value);

	Date getWLBlockingDate(JiraServiceContext srvContext, Long projectId);
	
	void setWLBlockingDate(JiraServiceContext srvContext, Long projectId, Date value);
	
	Date getWLWorklogBlockingDate(JiraServiceContext srvContext, Long projectId);
	
	void setWLWorklogBlockingDate(JiraServiceContext srvContext, Long projectId, Date value);
	
	Collection<ProjectRole> getProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId);
	
	void setProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId, Collection<ProjectRole> projectRoles);
}
