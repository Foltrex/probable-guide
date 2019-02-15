package com.scn.jira.worklog.core.wl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.worklog.Worklog;

import javax.inject.Named;

@ExportAsService({ExtendedWorklogManagerImpl.class })
@Named("extendedWorklogManagerImpl")
public class ExtendedWorklogManagerImpl implements ExtendedWorklogManager {

	public Worklog createExtWorklogType(Worklog worklog, String _worklogTypeId) throws DataAccessException
	{
		try
		{
			Map<String, Object> fields = UtilMisc.toMap("id", worklog.getId(), "worklogtype", _worklogTypeId);
			GenericValue gv = EntityUtils.createValue("WorklogExt", fields);
			gv.store();
		}
		catch (GenericEntityException e)
		{
			throw new DataAccessException(e);
		}
		return worklog;
	}
	
	public List<GenericValue> getExtWorklogsByType(String worklogTypeId)
	{
		List<GenericValue> result = ComponentAccessor.getOfBizDelegator().findByAnd("WorklogExt", UtilMisc.toMap("worklogtype", worklogTypeId));
		
		if (result == null)
			return Collections.emptyList();
		
		return result;
	}
	
	public void updateExtWorklogType(Long _worklogId, String _worklogType) throws DataAccessException
	{
		GenericValue xWorklogGV = getExtWorklog(_worklogId);
		try
		{
			if (xWorklogGV == null)
			{
				Map<String,Object> fields = UtilMisc.toMap("id", _worklogId, "worklogtype", _worklogType);
				xWorklogGV = EntityUtils.createValue("WorklogExt", fields);
			}
			else
			{
				xWorklogGV.set("worklogtype", _worklogType);
			}
			xWorklogGV.store();
		}
		catch (GenericEntityException e)
		{
			throw new DataAccessException("The error occured during updating an extended worklog.", e);
		}
	}
	
	public GenericValue getExtWorklog(Long _worklogId) throws DataAccessException
	{
		List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("WorklogExt", UtilMisc.toMap("id", _worklogId));
		
		GenericValue gv = null;
		if (worklogs != null && worklogs.size() > 0)
		{
			gv = (GenericValue) worklogs.get(0);
		}
		return gv;
	}
	
	public boolean deleteExtWorklogType(Long worklogId) throws DataAccessException
	{
		int i = ComponentAccessor.getOfBizDelegator().removeByAnd("WorklogExt", UtilMisc.toMap("id", worklogId));
		return i == 1;
	}
}
