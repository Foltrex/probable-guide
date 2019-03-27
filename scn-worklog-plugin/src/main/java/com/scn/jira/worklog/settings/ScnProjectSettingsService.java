package com.scn.jira.worklog.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 05.08.2010
 * Time: 14:05:40
 * To change this template use File | Settings | File Templates.
 */
@ExportAsService({ScnProjectSettingsService.class })
@Named("scnProjectSettingsService")
public class ScnProjectSettingsService implements IScnProjectSettingsService
{
	private final IScnProjectSettingsManager psManager;

	@Inject
	public ScnProjectSettingsService(IScnProjectSettingsManager psManager)
	{
		this.psManager = psManager;
	}
	
	public boolean isWLAutoCopyEnabled(JiraServiceContext srvContext, Long projectId)
	{
		if (isNotValid(srvContext, projectId)) return true;
		
		try
		{
			return psManager.isWLAutoCopyEnabled(projectId);
		}
    	catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
			return true;
		}
	}
	
	public void setWLAutoCopy(JiraServiceContext srvContext, Long projectId, boolean value)
	{
		if (isNotValid(srvContext, projectId)) return;
		
		try
		{
			psManager.setWLAutoCopy(projectId, value);
		}
    	catch (Exception e) 
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
		}
	}
	
	public Collection<WorklogType> getWorklogTypes(JiraServiceContext srvContext, Long projectId)
	{
		if (isNotValid(srvContext, projectId)) return Collections.emptyList();
		
		try
		{
			return psManager.getWorklogTypes(projectId);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
			return Collections.emptyList();
		}
	}
	
	public void setWorklogTypes(JiraServiceContext srvContext, Long projectId, Collection<WorklogType> worklogTypes)
	{
		if (isNotValid(srvContext, projectId)) return;
		
		try
		{
			psManager.setWorklogTypes(projectId, worklogTypes);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
		}
	}
	
	public boolean getUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId)
	{
		if (isNotValid(srvContext, projectId)) return false;
		
		try
		{
			return psManager.isUnspecifiedWLTypeAutoCopyEnabled(projectId);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
			return false;
		}
	}
	
	public void setUnspecifiedWorklogTypeOption(JiraServiceContext srvContext, Long projectId, boolean value)
	{
		if (isNotValid(srvContext, projectId)) return;
		
		try
		{
			psManager.setUnspecifiedWLTypeAutoCopyEnabled(projectId, String.valueOf(value));
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
		}
	}
	
	public boolean isWLTypeRequired(JiraServiceContext srvContext, Long projectId)
    {
    	if (isNotValid(srvContext, projectId)) return false;
    	
    	try 
    	{
    		return psManager.isWLTypeRequired(projectId);
    	} 
    	catch (Exception e) 
    	{
    		addErrorMessage(srvContext, "scn.transaction.failure");
    		return false;
    	}
    }
    
    public void setWLTypeRequired(JiraServiceContext srvContext, Long projectId, boolean value)
    {
    	if (isNotValid(srvContext, projectId)) return;
    	
    	try 
    	{
    		psManager.setWLTypeRequired(projectId, value);
    	} 
    	catch (Exception e) 
    	{
    		addErrorMessage(srvContext, "scn.transaction.failure");
    		return;
    	}
    }

    public Date getWLBlockingDate(JiraServiceContext srvContext, Long projectId)
	{
		if (isNotValid(srvContext, projectId)) return null;
		
		try
		{
			return psManager.getWLBlockingDate(projectId);
		}
    	catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
			return null;
		}
	}
	
	public void setWLBlockingDate(JiraServiceContext srvContext, Long projectId, Date value)
	{
		if (isNotValid(srvContext, projectId)) return;
		
		try
		{
			psManager.setWLBlockingDate(projectId, value);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
		}
	}
	
	public void setProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId, Collection<ProjectRole> projectRoles)
	{
		if (isNotValid(srvContext, projectId)) return;
		
		try
		{
			psManager.setProjectRolesToViewWL(projectId, projectRoles);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
		}
	}
	
	public Collection<ProjectRole> getProjectRolesToViewWL(JiraServiceContext srvContext, Long projectId)
	{
		if (isNotValid(srvContext, projectId)) return Collections.emptyList();
		
		try
		{
			return psManager.getProjectRolesToViewWL(projectId);
		}
		catch (Exception e)
		{
			addErrorMessage(srvContext, "scn.transaction.failure");
			return Collections.emptyList();
		}
	}
	
	private boolean isNotValid(JiraServiceContext srvContext, Long projectId)
	{
		Assertions.notNull("JiraServiceContext", srvContext);
		
		if (projectId == null)
		{
			addErrorMessage(srvContext, "scn.project_settings.error.project_id.null");
			return true;
		}
		
		return false;
	}
	
	private void addErrorMessage(JiraServiceContext srvContext, String key)
	{
		srvContext.getErrorCollection().addErrorMessage(srvContext.getI18nBean().getText(key));
	}
}