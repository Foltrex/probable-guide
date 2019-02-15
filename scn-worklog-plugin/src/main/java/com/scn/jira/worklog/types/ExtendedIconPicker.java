package com.scn.jira.worklog.types;

import com.atlassian.jira.config.ConstantsManager;
import java.util.Collection;
import java.util.Iterator;

import com.atlassian.jira.web.action.admin.IconPicker;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericValue;

public class ExtendedIconPicker extends IconPicker
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3759315530508755721L;
	
	protected ExtendedConstantsManager extendedConstantsManager;
	private final ConstantsManager constantsManager;
	private MultiMap issueConstants;
	
	public ExtendedIconPicker(
			ConstantsManager constantsManager,
			ExtendedConstantsManager extendedConstantsManager)
	{
		super(constantsManager);
		this.extendedConstantsManager = extendedConstantsManager;
		this.constantsManager = constantsManager;
	}
	
	public Collection getAssociatedImages(String imageLocation)
	{
		Collection associatedImages = null;
		String fieldType = getFieldType();
		if (fieldType.equals("worklogType"))
			associatedImages = (Collection) getIssueConstants().get(imageLocation);
		else
		{
			associatedImages = super.getAssociatedImages(imageLocation);
		}
		return associatedImages;
	}
	
	protected MultiMap getIssueConstants()
	{
		Iterator<GenericValue> iterator;
		if (this.issueConstants == null)
		{
			this.issueConstants = new MultiHashMap();
			
			Collection<GenericValue> fields = null;
			String fieldType = getFieldType();
			if (fieldType.equals("worklogType"))
				fields = this.extendedConstantsManager.getWorklogTypes();
			else
			{
				throw new IllegalArgumentException("Invalid field type selected.");
			}
			
			for (iterator = fields.iterator(); iterator.hasNext();)
			{
				GenericValue issueConstantGV = (GenericValue) iterator.next();
				String associatedImage = issueConstantGV.getString("iconurl");
				this.issueConstants.put(associatedImage, this.extendedConstantsManager.getIssueConstant(issueConstantGV));
			}
		}
		
		return this.issueConstants;
	}
}