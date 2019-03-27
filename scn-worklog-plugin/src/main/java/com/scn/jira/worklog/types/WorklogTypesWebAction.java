package com.scn.jira.worklog.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.util.TextUtils;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

@SuppressWarnings("rawtypes")
@Named("WorklogTypesWebAction")
public class WorklogTypesWebAction extends AbstractViewConstants<WorklogType> {
	private static final long serialVersionUID = -8127856504419827124L;

	private final OfBizDelegator ofBizDelegator;
	private final ExtendedConstantsManager ecManager;

	private boolean preview = false;
	private String statusColor;

	@Inject
	public WorklogTypesWebAction(@ComponentImport final TranslationManager translationManager,
			@ComponentImport final OfBizDelegator ofBizDelegator) {
		super(translationManager);

		this.ofBizDelegator = ofBizDelegator;
		this.ecManager = new DefaultExtendedConstantsManager();
	}

	protected String getConstantEntityName() {
		return "WorklogType";
	}

	protected String getNiceConstantName() {
		return "Worklog Type";
	}

	protected String getIssueConstantField() {
		return getJiraServiceContext().getI18nBean().getText("admin.issue.constant.worklogtype.lowercase");
	}

	protected WorklogType getConstant(String id) {
		return ecManager.getWorklogTypeObject(id);
	}

	protected String getRedirectPage() {
		return "ViewWorklogTypes.jspa";
	}

	protected Collection<WorklogType> getConstants() {
		return ecManager.getWorklogTypeObjects();
	}

	public Collection<WorklogType> getWorklogTypeObjects() {
		return ecManager.getWorklogTypeObjects();
	}

	protected void clearCaches() {
		ecManager.refreshWorklogTypes();
	}

	protected String redirectToView() {
		return getRedirect("ViewWorklogTypes.jspa");
	}

	protected String getDefaultPropertyName() {
		return "jira.constant.default.worklogtype";
	}

	public String doMoveDown() throws Exception {
		// TODO: implement!
		return "success";
	}

	public String doMoveUp() throws Exception {
		// TODO: implement!
		return "success";
	}

	public String doAddWorklogType() throws Exception {
		if (isPreview()) {
			return "input";
		}
		if (!TextUtils.stringSet(getIconurl())) {
			addError("iconurl",
					getJiraServiceContext().getI18nBean().getText("admin.errors.must.specify.url.for.icon.of.priority"));
		}
		if (!TextUtils.stringSet(getStatusColor())) {
			addError("statusColor", getJiraServiceContext().getI18nBean().getText("admin.errors.must.specify.color"));
		}
		addField("statusColor", getStatusColor());

		return super.doAddConstant();
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

	// public boolean isDefault(WorklogType worklogType) {
	// return isDefault(worklogType.getGenericValue());
	// }

	public String execute() throws Exception {
		if (!isSystemAdministrator()) {
			return getRedirect("/login.jsp?os_destination=" + getHttpRequest().getRequestURI());
		}
		return super.execute();
	}

	@Override
	protected void addConstant() throws GenericEntityException {
		Map<String, Object> dbParams = new HashMap<String, Object>();

		dbParams.put("name", getName());
		dbParams.put("description", getDescription());
		dbParams.put("iconurl", getIconurl());
		dbParams.put("statusColor", getStatusColor());

		this.ofBizDelegator.createValue(ExtendedConstantsManager.WORKLOGTYPE_CONSTANT_TYPE, dbParams);

		clearCaches();

		return;
	}
}