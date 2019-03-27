package com.scn.jira.worklog.panels;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.tabpanels.WorklogAction;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.WorklogType;

public class WorklogTabAction extends WorklogAction {

	private final ExtendedWorklogManager extWorklogManager;
	private final ExtendedConstantsManager extendedConstantsManager;
	
	public WorklogTabAction(
			IssueTabPanelModuleDescriptor descriptor, 
			Worklog worklog,
			JiraDurationUtils jiraDurationUtils, 
			boolean canEditWorklog, 
			boolean canDeleteWorklog,
			FieldLayoutManager fieldLayoutManager,
			RendererManager rendererManager,
			ExtendedWorklogManager extendedWorklogManager,
			ExtendedConstantsManager extendedConstantsManager,
			UserFormats userFormats) {
		super(descriptor, 
				worklog, 
				jiraDurationUtils, 
				canEditWorklog, 
				canDeleteWorklog, 
				fieldLayoutManager, 
				rendererManager,
				ComponentAccessor.getJiraAuthenticationContext().getLocale(), userFormats);
		
		this.extWorklogManager = extendedWorklogManager;
		this.extendedConstantsManager = extendedConstantsManager;
	}
	    
	public WorklogType getWorklogType() {
		try {
		    final GenericValue extWorklogGV = extWorklogManager.getExtWorklog(getWorklog().getId());
		    if (extWorklogGV != null) {
		    	return extendedConstantsManager.getWorklogTypeObject(extWorklogGV.getString("worklogtype"));
		    }
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	    return null;
	}
}
