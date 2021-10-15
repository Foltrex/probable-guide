package com.scn.jira.worklog.panels;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.issue.tabpanels.WorklogTabPanel;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.wl.ExtendedWorklogService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class OverridedWorklogTabPanel extends WorklogTabPanel {
    private final WorklogService worklogService;
    private final JiraDurationUtils jiraDurationUtils;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final IScnProjectSettingsManager psManager;
    private final ExtendedWorklogManager extendedWorklogManager;
    private final ExtendedConstantsManager extendedConstantsManager;
    private final UserFormats userFormats;
    private final ExtendedWorklogService extWorklogService;

    @Inject
    public OverridedWorklogTabPanel(ApplicationProperties applicationProperties, WorklogService worklogService,
                                    JiraDurationUtils jiraDurationUtils, FieldLayoutManager fieldLayoutManager,
                                    RendererManager rendererManager, IScnProjectSettingsManager scnProjectSettingsManager,
                                    ExtendedWorklogManager extendedWorklogManager, ExtendedConstantsManager extendedConstantsManager,
                                    UserFormats userFormats, FieldVisibilityManager fieldVisibilityManager, ExtendedWorklogService extWorklogService) {
        super(worklogService, jiraDurationUtils, fieldLayoutManager, rendererManager, applicationProperties, fieldVisibilityManager, userFormats);
        this.worklogService = worklogService;
        this.jiraDurationUtils = jiraDurationUtils;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.psManager = scnProjectSettingsManager;
        this.extendedWorklogManager = extendedWorklogManager;
        this.extendedConstantsManager = extendedConstantsManager;
        this.userFormats = userFormats;
        this.extWorklogService = extWorklogService;
    }

    @Override
    public List<IssueAction> getActions(Issue issue, ApplicationUser remoteUser) {
        List<IssueAction> worklogs = new ArrayList<>();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(remoteUser);

        for (Worklog worklog : worklogService.getByIssueVisibleToUser(context, issue)) {
            boolean blocked = extWorklogService.isDateExpired(context, worklog.getStartDate(), Objects.requireNonNull(worklog.getIssue().getProjectObject()),
                false);
            worklogs.add(new WorklogTabAction(descriptor, worklog, jiraDurationUtils, !blocked
                && worklogService.hasPermissionToUpdate(context, worklog), !blocked
                && worklogService.hasPermissionToDelete(context, worklog), fieldLayoutManager, rendererManager,
                extendedWorklogManager, extendedConstantsManager, userFormats));
        }

        if (worklogs.isEmpty()) {
            worklogs.add(new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nowork")));
        } else {
            worklogs.sort(IssueActionComparator.COMPARATOR);
        }

        return worklogs;
    }

    @Override
    public boolean showPanel(Issue issue, ApplicationUser user) {
        if (super.showPanel(issue, user)) {
            return psManager.hasPermissionToViewWL(user, issue.getProjectObject());
        } else {
            return false;
        }
    }
}
