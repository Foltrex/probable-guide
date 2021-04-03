package com.scn.jira.worklog.types;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.opensymphony.util.TextUtils;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.ofbiz.core.entity.GenericEntityException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Named
public class WorklogTypesWebAction extends AbstractViewConstants<WorklogType> {
    private static final long serialVersionUID = -8127856504419827124L;

    private final OfBizDelegator ofBizDelegator;
    private final ExtendedConstantsManager extendedConstantsManager;

    private boolean preview = false;
    private String statusColor;

    @Inject
    public WorklogTypesWebAction(TranslationManager translationManager, OfBizDelegator ofBizDelegator, ExtendedConstantsManager extendedConstantsManager) {
        super(translationManager);
        this.ofBizDelegator = ofBizDelegator;
        this.extendedConstantsManager = extendedConstantsManager;
    }

    @Override
    protected String getConstantEntityName() {
        return "WorklogType";
    }

    @Override
    protected String getNiceConstantName() {
        return "Worklog Type";
    }

    @Override
    protected String getIssueConstantField() {
        return getJiraServiceContext().getI18nBean().getText("admin.issue.constant.worklogtype.lowercase");
    }

    @Override
    protected WorklogType getConstant(String id) {
        return extendedConstantsManager.getWorklogTypeObject(id);
    }

    @Override
    protected String getRedirectPage() {
        return "ViewWorklogTypes.jspa";
    }

    @Override
    protected Collection<WorklogType> getConstants() {
        return extendedConstantsManager.getWorklogTypeObjects();
    }

    @Override
    protected void clearCaches() {
        extendedConstantsManager.refreshWorklogTypes();
    }

    @Override
    protected String redirectToView() {
        return getRedirect("ViewWorklogTypes.jspa");
    }

    @Override
    protected String getDefaultPropertyName() {
        return "jira.constant.default.worklogtype";
    }

    @Override
    protected void addConstant() throws GenericEntityException {
        Map<String, Object> dbParams = new HashMap<>();

        dbParams.put("name", getName());
        dbParams.put("description", getDescription());
        dbParams.put("iconurl", getIconurl());
        dbParams.put("statusColor", getStatusColor());

        this.ofBizDelegator.createValue(ExtendedConstantsManager.WORKLOGTYPE_CONSTANT_TYPE, dbParams);

        clearCaches();
    }

    @Override
    public String execute() throws Exception {
        if (!isSystemAdministrator()) {
            return getRedirect("/login.jsp?os_destination=" + getHttpRequest().getRequestURI());
        }
        return super.execute();
    }

    public Collection<WorklogType> getWorklogTypeObjects() {
        return extendedConstantsManager.getWorklogTypeObjects();
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
}
