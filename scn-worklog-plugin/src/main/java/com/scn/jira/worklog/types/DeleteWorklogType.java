package com.scn.jira.worklog.types;

import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericTransactionException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.WorklogType;

public class DeleteWorklogType extends AbstractDeleteConstant<WorklogType> {
	private static final long serialVersionUID = 8723911649418642629L;

	private final ExtendedConstantsManager extendedConstantsManager;
	private final IScnWorklogManager scnWorklogManager;
	private final ExtendedWorklogManager extWorklogManager;

	public DeleteWorklogType(ExtendedConstantsManager ecManager, IScnWorklogManager scnWorklogManager,
			ExtendedWorklogManager extWorklogManager) {
		this.extendedConstantsManager = ecManager;
		this.scnWorklogManager = scnWorklogManager;
		this.extWorklogManager = extWorklogManager;
	}

	protected String getConstantEntityName() {
		return "WorklogType";
	}

	protected String getNiceConstantName() {
		return getJiraServiceContext().getI18nBean().getText("admin.issue.constant.worklogtype.lowercase");
	}

	protected String getIssueConstantField() {
		return "worklogtype";
	}

	protected WorklogType getConstant(String id) {
		return this.extendedConstantsManager.getWorklogTypeObject(id);
	}

	protected String getRedirectPage() {
		return "ViewWorklogTypes.jspa";
	}

	protected Collection<WorklogType> getConstants() {
		return this.extendedConstantsManager.getWorklogTypeObjects();
	}

	protected void doValidation() {
		try {
			if (getConstant() == null) {
				this.log.error(getJiraServiceContext().getI18nBean().getText("admin.errors.no.constant.found",
						getNiceConstantName(), this.id));
			}
		} catch (Exception e) {
			this.log.error("Error occurred: " + e, e);
			addErrorMessage(getJiraServiceContext().getI18nBean().getText("admin.errors.general.error.occurred", e));
		}
	}

	protected String doExecute() throws Exception {
		String newWorklogTypeId = getNewId();
		String oldWorklogTypeId = getConstant().getId();
		if (isConfirm()) {
			try {
				boolean transactionStarted = CoreTransactionUtil.begin();
				try {
					for (GenericValue extWorklogGV : getMatchingExtWorklogs(oldWorklogTypeId)) {
						this.extWorklogManager.updateExtWorklogType(extWorklogGV.getLong("id"), newWorklogTypeId);
					}
					for (IScnWorklog scnWorklog : getMatchingScnWorklogs(oldWorklogTypeId)) {
						scnWorklog.setWorklogTypeId(newWorklogTypeId);
						this.scnWorklogManager.update(scnWorklog);
					}
					CoreTransactionUtil.commit(transactionStarted);

					// not compiled GenericValue constantGv = getConstant().getGenericValue();
					// String id = constantGv.getString("id");
					// constantGv.set("id", new Long(id));
					// OFBizPropertyUtils.removePropertySet(constantGv);
					// constantGv.set("id", id);
					// constantGv.remove();

					clearCaches();
					postProcess(oldWorklogTypeId);

				} catch (DataAccessException e) {
					CoreTransactionUtil.rollback(transactionStarted);
					getJiraServiceContext().getErrorCollection().addErrorMessage(
							getText(getJiraServiceContext(), "scn.transaction.failure"));
				}
			} catch (GenericTransactionException e) {
				getJiraServiceContext().getErrorCollection().addErrorMessage(
						getText(getJiraServiceContext(), "scn.transaction.failure"));
				this.log.error("Error occurred while rolling back transaction.", e);
			}
		}
		if (getHasErrorMessages()) {
			return "error";
		}
		return getRedirect(getRedirectPage());
	}

	public String execute() throws Exception {
		if (!isSystemAdministrator()) {
			return getRedirect("/login.jsp?os_destination=" + getHttpRequest().getRequestURI());
		}
		return super.execute();
	}

	protected void clearCaches() {
		this.extendedConstantsManager.refreshWorklogTypes();
	}

	protected void postProcess(String id) {
		if (id.equals(getApplicationProperties().getString("jira.constant.default.worklogtype"))) getApplicationProperties()
				.setString("jira.constant.default.worklogtype", null);
	}

	private List<GenericValue> getMatchingExtWorklogs(String worklogTypeId) {
		final List<GenericValue> result = this.extWorklogManager.getExtWorklogsByType(worklogTypeId);
		return result;
	}

	private List<IScnWorklog> getMatchingScnWorklogs(String worklogTypeId) {
		final List<IScnWorklog> result = this.scnWorklogManager.getScnWorklogsByType(worklogTypeId);
		return result;
	}

	public int getNumberMatchingWorklogs() throws Exception {
		String worklogTypeId = getConstant().getId();
		return getMatchingExtWorklogs(worklogTypeId).size() + getMatchingScnWorklogs(worklogTypeId).size();
	}

	private String getText(JiraServiceContext jiraServiceContext, String key) {
		return jiraServiceContext.getI18nBean().getText(key);
	}
}