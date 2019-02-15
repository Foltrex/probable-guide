package com.scn.jira.worklog.types;

import java.util.Collection;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.opensymphony.util.TextUtils;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

public class EditWorklogType extends AbstractEditConstant<WorklogType>
{
	private static final long serialVersionUID = 5144168438372178787L;
	
	private final OfBizDelegator ofBizDelegator;
	private final ExtendedConstantsManager ecManager;

    private boolean preview = false;
    private String statusColor;

    public EditWorklogType(
    		OfBizDelegator ofBizDelegator,
    		ExtendedConstantsManager ecManager) 
    {
    	this.ofBizDelegator = ofBizDelegator;
    	this.ecManager = ecManager;
    	// TODO
//        I18nBeanLocationHelper.addResourceBundle(this.i18nBean, JiraWebActionSupport.class.getName());
//        I18nBeanLocationHelper.addResourceBundle(this.i18nBean, "com.atlassian.jira.scn.administration.worklogtype.action.worklogsettings");
    }

    public String doDefault() throws Exception {
    	//not compiled setStatusColor(getConstant().getGenericValue().getString("statusColor"));
        return super.doDefault();
    }

    protected void doValidation() {
        if (!isPreview()) {
            if (!TextUtils.stringSet(getIconurl())) {
                addError("iconurl", getJiraServiceContext().getI18nBean().getText("admin.errors.must.specify.url.for.icon"));
            }
            if (!TextUtils.stringSet(getStatusColor())) {
                addError("statusColor", getJiraServiceContext().getI18nBean().getText("admin.errors.must.specify.color"));
            }
            if (getConstant() == null) {
                addErrorMessage(getJiraServiceContext().getI18nBean().getText("admin.errors.specified.constant.does.not.exist"));
            }

            if (!TextUtils.stringSet(getName())) {
                addError("name", getJiraServiceContext().getI18nBean().getText("admin.errors.must.specify.name"));
            }

            GenericValue constantByName = this.ecManager.getConstantByName(getConstantEntityName(), getName());
            if ((constantByName == null) || (constantByName.getString("id").equals(getConstant().getId())))
                return;
            addError("name", getJiraServiceContext().getI18nBean().getText("admin.errors.constant.already.exists", getNiceConstantName()));
        }
    }

    protected String doExecute() throws Exception {
        if (isPreview()) {
            return INPUT;
        }
        
        updateConstant();
        
        return getRedirect(getRedirectPage());
    }

    public String execute() throws Exception {
        if (!isSystemAdministrator()) {
            return getRedirect("/login.jsp?os_destination=" + getHttpRequest().getRequestURI());
        }
        return super.execute();
    }

    protected String getConstantEntityName() {
        return "WorklogType";
    }

    protected String getNiceConstantName() {
        return getJiraServiceContext().getI18nBean().getText("admin.issue.constant.worklogtype.lowercase");
    }

    protected String getIssueConstantField() {
        return "Worklog Type";
    }

    protected WorklogType getConstant(String id) {
        return this.ecManager.getWorklogTypeObject(id);
    }

    protected String getRedirectPage() {
        return "ViewWorklogTypes.jspa";
    }

    protected Collection<WorklogType> getConstants() {
        return this.ecManager.getWorklogTypeObjects();
    }

    protected void clearCaches() {
        this.ecManager.refreshWorklogTypes();
    }

    public boolean isPreview() {
        return this.preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public String getStatusColor() {
        return this.statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
    
	protected GenericValue updateConstant() throws GenericEntityException
	{
		//not compiled GenericValue gv = getConstant().getGenericValue();
		
//		gv.set("name", getName());
//		gv.set("description", getDescription());
//		gv.set("iconurl", getIconurl());
//		gv.set("statusColor", getStatusColor());
//		
//		this.ofBizDelegator.store(gv);
//
//		clearCaches();
//		
//		return gv;
		return null;
	}
}