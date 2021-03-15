package com.scn.jira.worklog.wl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResultFactory;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mention.MentionService;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.issue.CreateWorklog;
import com.atlassian.jira.web.action.issue.util.AttachmentHelper;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtendedCreateWorklog extends CreateWorklog {
    private static final long serialVersionUID = -4594446887729353135L;

    protected final WorklogService worklogService;
    protected final ExtendedWorklogService extWorklogService;
    private final WorklogManager worklogManager;
    private final JiraAuthenticationContext authenticationContext;
    private final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();
    private final ExtendedConstantsManager extendedConstantsManager;
    private final IScnProjectSettingsManager psManager;

    private String worklogType;
    private String inputReporter;
    private Worklog worklog;
    private Long newEstimateLong;
    private Long adjustmentAmountLong;

    public ExtendedCreateWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager,
                                 DateTimeFormatterFactory dateTimeFormatterFactory, FieldVisibilityManager fieldVisibilityManager,
                                 FieldLayoutManager fieldLayoutManager, RendererManager rendererManager, UserUtil userUtil,
                                 @Qualifier("overridedWorklogManager") WorklogManager worklogManager,
                                 JiraAuthenticationContext authenticationContext, FieldScreenRendererFactory fieldScreenRendererFactory,
                                 FieldManager fieldManager, SubTaskManager subTaskManager, AttachmentHelper attachmentHelper,
                                 MentionService mentionService, IScnProjectSettingsManager scnProjectSettingsManager,
                                 ExtendedWorklogService extendedWorklogService, JiraDurationUtils jiraDurationUtils,
                                 ExtendedConstantsManager extendedConstantsManager) {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, dateTimeFormatterFactory, fieldVisibilityManager, fieldLayoutManager,
            rendererManager, userUtil, fieldScreenRendererFactory, fieldManager, subTaskManager, attachmentHelper, mentionService);
        this.worklogService = worklogService;
        this.worklogManager = worklogManager;
        this.authenticationContext = authenticationContext;
        this.extWorklogService = extendedWorklogService;
        this.extendedConstantsManager = extendedConstantsManager;
        this.psManager = scnProjectSettingsManager;
    }

    public boolean shouldDisplay() {
        return isIssueValid() && /*hasIssuePermission("work", getIssueObject()) &&*/ !isTimeTrackingFieldHidden(getIssueObject())
            && isWorkflowAllowsEdit(getIssueObject())
            && psManager.hasPermissionToViewWL(getLoggedInApplicationUser(), getIssueObject().getProjectObject());
    }

    public String doDefault() throws Exception {
        if (!this.worklogService.hasPermissionToCreate(getJiraServiceContext(), getIssueObject(), false)) {
            return "securitybreach";
        }
        WorklogType defaultWorklogType = psManager.getDefaultWorklogType(
            Objects.requireNonNull(getIssueObject().getProjectObject()).getId()
        );
        this.setWorklogType(defaultWorklogType != null ? defaultWorklogType.getId() : "");
        this.setStartDate(this.getFormattedStartDate(new Date()));

        return "input";
    }

    protected String doExecute() throws Exception {
        if (isTimeTrackingFieldHidden(getIssueObject())) {
            return "securitybreach";
        }

        if ("auto".equalsIgnoreCase(this.adjustEstimate)) this.worklog = this.worklogService
            .createAndAutoAdjustRemainingEstimate(getJiraServiceContext(), WorklogResultFactory.create(this.worklog), true);
        else if ("new".equalsIgnoreCase(this.adjustEstimate))
            this.worklog = this.worklogService.createWithNewRemainingEstimate(
                getJiraServiceContext(), WorklogResultFactory.createNewEstimate(this.worklog, this.newEstimateLong), true);
        else if ("manual".equalsIgnoreCase(this.adjustEstimate)) this.worklog = this.worklogService
            .createWithManuallyAdjustedEstimate(getJiraServiceContext(),
                WorklogResultFactory.createAdjustmentAmount(this.worklog, this.adjustmentAmountLong), true);
        else this.worklog = this.worklogService.createAndRetainRemainingEstimate(getJiraServiceContext(),
                WorklogResultFactory.create(this.worklog), true);

        if (getHasErrorMessages()) {
            return "error";
        }

        if (!getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
            this.extWorklogService.createWorklogType(getJiraServiceContext(), getWorklog(), getWorklogType());
        }

        if (isInlineDialogMode()) {
            return returnComplete();
        }

        return getRedirect("/browse/" + getIssue().getString("key"));
    }

    protected void doValidation() {
        if (!isIssueValid()) return;

        CommentVisibility visibility = getCommentVisibility();
        WorklogInputParametersImpl.Builder paramBuilder = WorklogInputParametersImpl.builder().issue(getIssueObject())
            .timeSpent(getTimeLogged()).startDate(getParsedStartDate()).comment(getComment())
            .groupLevel(visibility.getGroupLevel()).roleLevelId(visibility.getRoleLevel());

        if ("new".equalsIgnoreCase(this.adjustEstimate)) {
            WorklogNewEstimateResult worklogNewEstimateResult = this.worklogService.validateCreateWithNewEstimate(
                getJiraServiceContext(), paramBuilder.newEstimate(getNewEstimate()).buildNewEstimate());

            if (worklogNewEstimateResult != null) {
                this.worklog = worklogNewEstimateResult.getWorklog();
                this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else if ("manual".equalsIgnoreCase(this.adjustEstimate)) {
            WorklogAdjustmentAmountResult worklogAdjustmentAmountResult = this.worklogService
                .validateCreateWithManuallyAdjustedEstimate(getJiraServiceContext(),
                    paramBuilder.adjustmentAmount(getAdjustmentAmount()).buildAdjustmentAmount());

            if (worklogAdjustmentAmountResult != null) {
                this.worklog = worklogAdjustmentAmountResult.getWorklog();
                this.adjustmentAmountLong = worklogAdjustmentAmountResult.getAdjustmentAmount();
            }
        } else {
            WorklogResult worklogResult = this.worklogService.validateCreate(getJiraServiceContext(), paramBuilder.build());

            if (worklogResult != null) {
                this.worklog = worklogResult.getWorklog();
            }
        }

        if (psManager.isWLTypeRequired(getIssueObject().getProjectObject().getId()) && StringUtils.isBlank(getWorklogType()))
            getJiraServiceContext()
                .getErrorCollection().addError("worklogType",
                getJiraServiceContext().getI18nBean().getText("logwork.worklogtype.error.null"));

        if (extWorklogService.isDateExpired(getJiraServiceContext(), getParsedStartDate(),
            getIssueObject().getProjectObject(), false))
            return;

        ApplicationUser reporter = null;
        if (!StringUtils.isBlank(getInputReporter())) {
            reporter = ComponentAccessor.getUserManager().getUserByKey(getInputReporter());
        }

        if (reporter == null) {
            getJiraServiceContext().getErrorCollection().addError("inputReporter",
                getJiraServiceContext().getI18nBean().getText("logwork.reporter.error.null"));
        } else if (this.worklog != null) this.worklog = reassignWorklog(this.worklog, reporter);
    }

    protected Worklog reassignWorklog(Worklog worklog, ApplicationUser reporter) {
        assert (worklog != null);
        assert (reporter != null);

        Worklog reassignedWorklog = new WorklogImpl(this.worklogManager, worklog.getIssue(), worklog.getId(), reporter.getKey(),
            worklog.getComment(), worklog.getStartDate(), worklog.getGroupLevel(), worklog.getRoleLevelId(),
            worklog.getTimeSpent(), getJiraServiceContext().getLoggedInApplicationUser().getKey(), worklog.getCreated(),
            worklog.getUpdated());

        return reassignedWorklog;
    }

    public Map<String, String> getAssignableUsers() {
        try {
            List<ApplicationUser> users = Lists.newArrayList(ComponentAccessor.getPermissionSchemeManager().getUsers(
                ProjectPermissions.WORK_ON_ISSUES, ComponentAccessor.getPermissionContextFactory().getPermissionContext(getIssueObject())));

            if (CollectionUtils.isEmpty(users)) return Collections.emptyMap();

            users.sort(new UserBestNameComparator(getJiraServiceContext().getI18nBean().getLocale()));

            Map<String, String> assignableUsers = new ListOrderedMap();
            for (ApplicationUser user : users) {
                assignableUsers.put(user.getKey(), user.getDisplayName());
            }
            return assignableUsers;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
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
        Collection<WorklogType> worklogTypes = this.extendedConstantsManager.getWorklogTypeObjects();
        Collection<WorklogType> excludedWorklogTypes = this.psManager.getExcludedWorklogTypes(Objects.requireNonNull(getIssueObject().getProjectObject()).getId());
        excludedWorklogTypes.forEach(worklogTypes::remove);

        return worklogTypes;
    }

    public boolean isWorklogTypeSelected(String worklogType) {
        return (getWorklogType() != null) && (getWorklogType().equals(worklogType));
    }

    public CalendarResourceIncluder getCalendarIncluder() {
        return this.calendarResourceIncluder;
    }

    public boolean getHasCalendarTranslation() {
        return this.calendarResourceIncluder.hasTranslation(this.authenticationContext.getLocale());
    }

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public Calendar getCurrentCalendar() {
        return Calendar.getInstance(this.authenticationContext.getLocale());
    }

    public String getModifierKey() {
        return BrowserUtils.getModifierKey();
    }

    public String getSuperActionName() {
        return ExtendedCreateWorklog.class.getSimpleName();
    }

    public String getInputReporter() {
        return this.inputReporter;
    }

    public void setInputReporter(String inputReporter) {
        this.inputReporter = inputReporter;
    }

    public boolean isWlTypeRequired() {
        return psManager.isWLTypeRequired(getIssueObject().getProjectObject().getId());
    }

    public Worklog getWorklog() {
        return this.worklog;
    }
}
