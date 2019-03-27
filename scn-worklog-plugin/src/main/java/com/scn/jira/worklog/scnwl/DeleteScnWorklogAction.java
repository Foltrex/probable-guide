package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 09.08.2010
 * Time: 11:58:41
 * To change this template use File | Settings | File Templates.
 */
@Named
public class DeleteScnWorklogAction extends AbstractScnWorklogAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7662358199449964631L;
	
	private IScnWorklog worklog;
    private Long newEstimateLong;
    private Long adjustmentAmountLong;

    @Inject
    public DeleteScnWorklogAction(@ComponentImport CommentService commentService,
    		ProjectRoleManager projectRoleManager,
    		GroupManager groupManager,
    		IScnExtendedIssueStore extIssueStore, 
    		IScnWorklogService scnWorklogService,
    		IScnProjectSettingsManager projectSettignsManager,
    		ExtendedConstantsManager extendedConstantsManager) 
    {
    	super(commentService, 
    			projectRoleManager, 
    			ComponentAccessor.getComponent(JiraDurationUtils.class), 
    			groupManager, 
    			extIssueStore, 
    			scnWorklogService,
    			projectSettignsManager, 
    			extendedConstantsManager);
    }

    public String doDefault() throws Exception {
        this.worklog = this.scnWorklogService.getById(getJiraServiceContext(), getWorklogId());
        if (this.worklog == null) {
            addErrorMessage(getJiraServiceContext().getI18nBean().getText("logwork.error.update.invalid.id", (getWorklogId() == null) ? null : getWorklogId().toString()));
            return "error";
        }
        if (!this.scnWorklogService.hasPermissionToDelete(getJiraServiceContext(), this.worklog)) {
            return "securitybreach";
        }
        setWorklogType(this.worklog.getWorklogTypeId());
        return super.doDefault();
    }

    public void doValidation() {
    	if ("new".equalsIgnoreCase(this.adjustEstimate)) {
            IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = this.scnWorklogService.validateDeleteWithNewEstimate(getJiraServiceContext(), getWorklogId(), getNewEstimate());

            if (worklogNewEstimateResult != null) {
                this.worklog = worklogNewEstimateResult.getWorklog();
                this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else if ("manual".equalsIgnoreCase(this.adjustEstimate)) {
            IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult = this.scnWorklogService.validateDeleteWithManuallyAdjustedEstimate(getJiraServiceContext(), getWorklogId(), getAdjustmentAmount());

            if (worklogAdjustmentAmountResult != null) {
                this.worklog = worklogAdjustmentAmountResult.getWorklog();
                this.adjustmentAmountLong = worklogAdjustmentAmountResult.getAdjustmentAmount();
            }
        } else {
            this.worklog = this.scnWorklogService.validateDelete(getJiraServiceContext(), getWorklogId());
        }
    }

    public String doExecute()
            throws Exception {
    	setWorklogType(this.worklog.getWorklogTypeId());
        if ("auto".equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteAndAutoAdjustRemainingEstimate(getJiraServiceContext(), this.worklog, true, isWlAutoCopy());
        } else if ("new".equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteWithNewRemainingEstimate(getJiraServiceContext(), new IScnWorklogService.WorklogNewEstimateResult(this.worklog, this.newEstimateLong), true, isWlAutoCopy());
        } else if ("manual".equalsIgnoreCase(this.adjustEstimate)) {
            this.scnWorklogService.deleteWithManuallyAdjustedEstimate(getJiraServiceContext(), new IScnWorklogService.WorklogAdjustmentAmountResult(this.worklog, this.adjustmentAmountLong), true, isWlAutoCopy());
        } else {
            this.scnWorklogService.deleteAndRetainRemainingEstimate(getJiraServiceContext(), this.worklog, true, isWlAutoCopy());
        }

        if (getHasErrorMessages()) {
            return "error";
        }

        if(isInlineDialogMode())
		{
			return returnComplete();
		}
        
        return getRedirect("/browse/" + getIssue().getString("key"));
    }

    public IScnWorklog getWorklog() {
        return this.worklog;
    }
    
    public boolean isWlAutoCopyDisabled()
    {
    	return getWorklog() == null || getWorklog().getLinkedWorklog() == null;
    }
    
    public boolean isWlAutoCopyChecked()
    {
    	if(getJiraServiceContext().getErrorCollection().hasAnyErrors())
    		return isWlAutoCopy();
    	
    	if (this.worklog.getLinkedWorklog() == null){
    		return false;
    	} else {
    		return true;
    	}
    	
    	//if(getWorklogAutoCopyOption())
    		//return getWorklogTypeIsChecked(getWorklogType());
    	
    	//return false;
    }
    
}
