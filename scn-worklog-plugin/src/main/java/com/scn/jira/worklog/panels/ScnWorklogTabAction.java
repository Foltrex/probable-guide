package com.scn.jira.worklog.panels;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

public class ScnWorklogTabAction extends AbstractIssueAction {
    private static final Logger log = Logger.getLogger(ScnWorklogTabAction.class);
    private final ExtendedConstantsManager extendedConstantsManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final Issue issue;
    private final String worklogTypeId;
    private final boolean canDisplayActionAllTab;
    private final boolean canEditWorklog;
    private final boolean canDeleteWorklog;
    private final Worklog worklog;
    private final JiraDurationUtils jiraDurationUtils;
    private final UserFormats userFormats;

    public ScnWorklogTabAction(IssueTabPanelModuleDescriptor descriptor, Worklog worklog, String worklogTypeId,
                               JiraDurationUtils jiraDurationUtils, ExtendedConstantsManager extendedConstantsManager,
                               boolean canEditWorklog, boolean canDeleteWorklog, boolean canDisplayActionAllTab,
                               FieldLayoutManager fieldLayoutManager, RendererManager rendererManager
            , UserFormats userFormats) {
        super(descriptor);
        this.canDisplayActionAllTab = canDisplayActionAllTab;
        this.canDeleteWorklog = canDeleteWorklog;
        this.canEditWorklog = canEditWorklog;
        this.worklog = worklog;
        this.worklogTypeId = worklogTypeId;
        this.jiraDurationUtils = jiraDurationUtils;
        this.extendedConstantsManager = extendedConstantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.issue = worklog.getIssue();
        this.userFormats = userFormats;
    }

    public Date getTimePerformed() {
        return worklog.getStartDate();
    }

    protected void populateVelocityParams(Map params) {
        params.put("action", this);
        params.put("worklog", getWorklog());
        params.put("worklogType", getWorklogType());
        params.put("content", this.worklog.getComment());
        params.put("userformats", this.userFormats);
        try {
            FieldLayoutItem fieldLayoutItem = this.fieldLayoutManager
                    .getFieldLayout(this.issue).getFieldLayoutItem("worklog");
            if (fieldLayoutItem != null) {
                params.put(
                        "content",
                        this.rendererManager.getRenderedContent(
                                fieldLayoutItem.getRendererType(),
                                this.worklog.getComment(),
                                this.issue.getIssueRenderContext()));
            }
        } catch (DataAccessException e) {
            log.error(e);
        }
    }

    public String getPrettyDuration(String duration) {
        return jiraDurationUtils.getFormattedDuration(new Long(duration));
    }

    public Worklog getWorklog() {
        return worklog;
    }

    public boolean isCanDeleteWorklog() {
        return canDeleteWorklog;
    }

    public boolean isCanEditWorklog() {
        return canEditWorklog;
    }

    public boolean isDisplayActionAllTab() {
        return false;
    }

    public WorklogType getWorklogType() {
        try {
            if (worklogTypeId != null) {
                return extendedConstantsManager.getWorklogTypeObject(worklogTypeId);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
