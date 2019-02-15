package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.issue.IssueConstant;

import java.util.Collection;
import org.ofbiz.core.entity.GenericValue;

public interface ExtendedConstantsManager
{
	public static final String WORKLOGTYPE_CONSTANT_TYPE = "WorklogType";
	
	public WorklogType getWorklogTypeObject(String paramString);
	
	public GenericValue getWorklogType(String paramString);
	
	public Collection<GenericValue> getWorklogTypes();
	
	public Collection<WorklogType> getWorklogTypeObjects();
	
	public void refreshWorklogTypes();
	
	public IssueConstant getIssueConstant(GenericValue paramGenericValue);
	
	public GenericValue getConstant(String paramString1, String paramString2);
	
	public IssueConstant getConstantObject(String paramString1, String paramString2);
	
	public Collection getConstantObjects(String paramString);
	
	public GenericValue getConstantByName(String paramString1, String paramString2);
}