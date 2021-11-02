package com.scn.jira.worklog.wl;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.comment.CommentService;
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
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.issue.UpdateWorklog;
import com.atlassian.jira.web.action.issue.util.AttachmentHelper;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtendedUpdateWorklog extends UpdateWorklog {
    private static final long serialVersionUID = -7219473321948115788L;

    private final WorklogManager worklogManager;
    private final WorklogService worklogService;
    private final JiraAuthenticationContext authenticationContext;
    protected final ExtendedConstantsManager extendedConstantsManager;
    protected final ExtendedWorklogService extWorklogService;
    private final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();
    private final IScnProjectSettingsManager psManager;
    private String worklogType;
    private String inputReporter;
    private Long newEstimateLong;
    private Worklog worklog;

    public ExtendedUpdateWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager,
                                 DateTimeFormatterFactory dateTimeFormatterFactory, FieldVisibilityManager fieldVisibilityManager,
                                 FieldLayoutManager fieldLayoutManager, RendererManager rendererManager, UserUtil userUtil,
                                 @Qualifier("overridedWorklogManager") WorklogManager worklogManager,
                                 JiraAuthenticationContext authenticationContext, SubTaskManager subTaskManager,
                                 FieldScreenRendererFactory fieldScreenRendererFactory, FieldManager fieldManager,
                                 AttachmentHelper attachmentHelper, MentionService mentionService, JiraDurationUtils jiraDurationUtils,
                                 ExtendedWorklogService extendedWorklogService, ExtendedConstantsManager extendedConstantsManager,
                                 IScnProjectSettingsManager psManager) {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, dateTimeFormatterFactory, fieldVisibilityManager,
            fieldLayoutManager, rendererManager, userUtil, subTaskManager, fieldScreenRendererFactory, fieldManager, attachmentHelper, mentionService);
        this.worklogManager = worklogManager;
        this.worklogService = worklogService;
        this.authenticationContext = authenticationContext;
        this.extWorklogService = extendedWorklogService;
        this.extendedConstantsManager = extendedConstantsManager;
        this.psManager = psManager;
    }

    public boolean shouldDisplay() {
        return isIssueValid() && /*hasIssuePermission("work", getIssueObject()) &&*/ !isTimeTrackingFieldHidden(getIssueObject())
            && isWorkflowAllowsEdit(getIssueObject())
            && psManager.hasPermissionToViewWL(getLoggedInUser(), getIssueObject().getProjectObject());
    }

    public String doDefault() throws Exception {
        GenericValue extWorklogGV = this.extWorklogService.getExtWorklogById(getJiraServiceContext(), getWorklogId());
        if (extWorklogGV != null) {
            setWorklogType(extWorklogGV.getString("worklogtype"));
        }

        this.worklog = this.worklogService.getById(getJiraServiceContext(), getWorklogId());
        if (this.worklog == null) {
            addErrorMessage(getJiraServiceContext().getI18nBean().getText("logwork.error.update.invalid.id",
                getWorklogId() == null ? null : getWorklogId().toString()));
            return "error";
        }
        if (!this.worklogService.hasPermissionToUpdate(getJiraServiceContext(), this.worklog)) {
            return "securitybreach";
        }

        setTimeLogged(DateUtils.getDurationString(this.worklog.getTimeSpent(), getHoursPerDay().intValue(),
            getDaysPerWeek().intValue()));
        setStartDate(getFormattedStartDate(this.worklog.getStartDate()));
        setComment(this.worklog.getComment());
        setCommentLevel(CommentVisibility.getCommentLevelFromLevels(this.worklog.getGroupLevel(), this.worklog.getRoleLevelId()));

        return "input";
    }

    public void doValidation() {
        if (extWorklogService.isDateExpired(getJiraServiceContext(), worklogManager.getById(getWorklogId()).getStartDate(),
            Objects.requireNonNull(getIssueObject().getProjectObject()), false)) return;

        CommentVisibility visibility = getCommentVisibility();
        WorklogInputParametersImpl.Builder paramBuilder = WorklogInputParametersImpl.builder().worklogId(getWorklogId())
            .timeSpent(getTimeLogged()).startDate(getParsedStartDate()).comment(getComment())
            .groupLevel(visibility.getGroupLevel()).roleLevelId(visibility.getRoleLevel());

        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
            WorklogNewEstimateResult worklogNewEstimateResult = this.worklogService.validateUpdateWithNewEstimate(
                getJiraServiceContext(), paramBuilder.newEstimate(getNewEstimate()).buildNewEstimate());

            if (worklogNewEstimateResult != null) {
                this.worklog = worklogNewEstimateResult.getWorklog();
                this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
            }
        } else {
            WorklogResult worklogResult = this.worklogService.validateUpdate(getJiraServiceContext(), paramBuilder.build());

            if (worklogResult != null) {
                this.worklog = worklogResult.getWorklog();
            }
        }

        if (psManager.isWLTypeRequired(getIssueObject().getProjectObject().getId()) && StringUtils.isBlank(getWorklogType()))
            getJiraServiceContext()
                .getErrorCollection().addError("worklogType",
                    getJiraServiceContext().getI18nBean().getText("logwork.worklogtype.error.null"));

        if (extWorklogService.isDateExpired(getJiraServiceContext(), getParsedStartDate(), getIssueObject().getProjectObject(),
            false)) return;

        ApplicationUser reporter = null;
        if (!StringUtils.isBlank(getInputReporter())) {
            reporter = ComponentAccessor.getUserManager().getUserByKey(getInputReporter());
        }

        if (reporter == null) {
            getJiraServiceContext().getErrorCollection().addError("inputReporter",
                getJiraServiceContext().getI18nBean().getText("logwork.reporter.error.null"));
        } else if (this.worklog != null) this.worklog = reassignWorklog(this.worklog, reporter);
    }

    public String doExecute() throws Exception {
        if ("auto".equalsIgnoreCase(this.adjustEstimate)) this.worklogService.updateAndAutoAdjustRemainingEstimate(
            getJiraServiceContext(), WorklogResultFactory.create(this.worklog), true);
        else if ("new".equalsIgnoreCase(this.adjustEstimate)) this.worklogService.updateWithNewRemainingEstimate(
            getJiraServiceContext(), WorklogResultFactory.createNewEstimate(this.worklog, this.newEstimateLong), true);
        else {
            this.worklogService.updateAndRetainRemainingEstimate(getJiraServiceContext(),
                WorklogResultFactory.create(this.worklog), true);
        }

        if (getHasErrorMessages()) {
            return "error";
        }

        if (!getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
            this.extWorklogService.updateWorklogType(getJiraServiceContext(), getWorklogId(), getWorklogType());
        }

        if (isInlineDialogMode()) {
            return returnComplete();
        }

        return getRedirect("/browse/" + getIssue().getString("key"));
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
                new Long(17L),
                getIssueObject() != null ? ComponentAccessor.getPermissionContextFactory().getPermissionContext(
                    getIssueObject()) : ComponentAccessor.getPermissionContextFactory().getPermissionContext(
                    getSelectedProjectObject())));

            if ((users == null) || (users.isEmpty())) {
                return Collections.emptyMap();
            }
            Map<String, String> assignableUsers = new ListOrderedMap();

            Collections.sort(users, new UserBestNameComparator(getJiraServiceContext().getI18nBean().getLocale()));

            for (Iterator<ApplicationUser> iterator = users.iterator(); iterator.hasNext(); ) {
                ApplicationUser user = iterator.next();
                assignableUsers.put(user.getKey(), user.getDisplayName());
            }
            return assignableUsers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
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
        Collection<WorklogType> excludedWorklogTypes = this.psManager.getExcludedWorklogTypes(Objects.requireNonNull(getIssueObject().getProjectObject()).getId());
        return this.extendedConstantsManager.getWorklogTypeObjects().stream()
            .filter(worklogTypeObject -> !excludedWorklogTypes.contains(worklogTypeObject))
            .collect(Collectors.toList());
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
        String classname = super.getClass().getSuperclass().getName();
        return classname.substring(classname.lastIndexOf('.') + 1);
    }

    public Worklog getWorklog() {
        return this.worklog;
    }

    public boolean isWlTypeRequired() {
        return psManager.isWLTypeRequired(getIssueObject().getProjectObject().getId());
    }

    public String getInputReporter() {
        return this.inputReporter;
    }

    public void setInputReporter(String inputReporter) {
        this.inputReporter = inputReporter;
    }
}
