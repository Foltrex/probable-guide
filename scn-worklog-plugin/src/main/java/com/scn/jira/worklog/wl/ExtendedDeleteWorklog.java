package com.scn.jira.worklog.wl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mention.MentionService;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.issue.DeleteWorklog;
import com.atlassian.jira.web.action.issue.util.AttachmentHelper;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.Objects;

public class ExtendedDeleteWorklog extends DeleteWorklog {
    private static final long serialVersionUID = 1L;

    protected final ExtendedConstantsManager extendedConstantsManager;
    protected final ExtendedWorklogService worklogService;
    private final IScnProjectSettingsManager psManager;

    private String worklogType;

    public ExtendedDeleteWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager,
                                 DateTimeFormatterFactory dateTimeFormatterFactory, FieldVisibilityManager fieldVisibilityManager,
                                 FieldLayoutManager fieldLayoutManager, RendererManager rendererManager,
                                 @Qualifier("overridedWorklogManager") WorklogManager worklogManager, UserUtil userUtil,
                                 JiraDurationUtils jiraDurationUtils, SubTaskManager subTaskManager, FieldScreenRendererFactory fieldScreenRendererFactory,
                                 FieldManager fieldManager, AttachmentHelper attachmentHelper, MentionService mentionService) {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, dateTimeFormatterFactory, fieldVisibilityManager, fieldLayoutManager, rendererManager, worklogManager, userUtil,
            subTaskManager, fieldScreenRendererFactory, fieldManager, attachmentHelper, mentionService);
        this.worklogService = new ExtendedWorklogService(new ExtendedWorklogManagerImpl(), new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager()));
        this.extendedConstantsManager = new DefaultExtendedConstantsManager();
        this.psManager = new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager());
    }

    public boolean shouldDisplay() {
        return isIssueValid() && hasIssuePermission("work", getIssueObject()) && !isTimeTrackingFieldHidden(getIssueObject())
            && isWorkflowAllowsEdit(getIssueObject())
            && psManager.hasPermissionToViewWL(getLoggedInUser(), getIssueObject().getProjectObject());
    }

    @Override
    public void doValidation() {
        if (worklogService.isDateExpired(getJiraServiceContext(), getParsedStartDate(),
            Objects.requireNonNull(getIssueObject().getProjectObject()), true))
            return;

        super.doValidation();
    }

    public String doExecute() throws Exception {
        String redirect = super.doExecute();
        if (!getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
            this.worklogService.deleteWorklogType(getJiraServiceContext(), getWorklogId());
        }
        return redirect;
    }

    public String getWorklogType() {
        if (this.worklogType == null) {
            return "";
        }
        return this.worklogType;
    }

    public void setWorklogType(String worklogType) {
        this.worklogType = worklogType;
    }

    public Collection<WorklogType> getWorklogTypeObjects() {
        return extendedConstantsManager.getWorklogTypeObjects();
    }

    public String getModifierKey() {
        return BrowserUtils.getModifierKey();
    }
}
