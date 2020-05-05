package com.scn.jira.worklog;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.*;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import org.springframework.stereotype.Component;

@Component
public class PluginComponentImpl implements PluginComponent {
    @ComponentImport
    private ActiveObjects activeObjects;
    @ComponentImport
    private AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    @ComponentImport
    private ApplicationProperties applicationProperties;
    @ComponentImport
    private BeanFactory beanFactory;
    @ComponentImport
    private CommentService commentService;
    @ComponentImport
    private ConstantsManager constantsManager;
    @ComponentImport
    private DateTimeFormatterFactory dateTimeFormatterFactory;
    @ComponentImport
    private EventPublisher eventPublisher;
    @ComponentImport
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    @ComponentImport
    private FieldLayoutManager fieldLayoutManager;
    @ComponentImport
    private FieldManager fieldManager;
    @ComponentImport
    private FieldVisibilityManager fieldVisibilityManager;
    @ComponentImport
    private FeatureManager featureManager;
    @ComponentImport
    private GlobalPermissionManager globalPermissionManager;
    @ComponentImport
    private GroupManager groupManager;
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private IssueTypeSchemeManager issueTypeSchemeManager;
    @ComponentImport
    private IssueTypeSchemeService issueTypeSchemeService;
    @ComponentImport
    private IssueEventBundleFactory issueEventBundleFactory;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    private JqlLocalDateSupport jqlLocalDateSupport;
    @ComponentImport
    private JqlDateSupport jqlDateSupport;
    @ComponentImport
    private LocaleManager localeManager;
    @ComponentImport
    private OfBizDelegator ofBizDelegator;
    @ComponentImport
    private OptionSetManager optionSetManager;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private ProjectRoleManager projectRoleManager;
    @ComponentImport
    private PluginScheduler pluginScheduler;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private PluginAccessor pluginAccessor;
    @ComponentImport
    private TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory;
    @ComponentImport
    private TimeTrackingConfiguration timeTrackingConfiguration;
    @ComponentImport
    private TimeTrackingIssueUpdater timeTrackingIssueUpdater;
    @ComponentImport
    private TranslationManager translationManager;
    @ComponentImport
    private SubTaskManager subTaskManager;
    @ComponentImport
    private SearchProvider searchProvider;
    @ComponentImport
    private VelocityRequestContextFactory velocityRequestContextFactory;
    @ComponentImport
    private VelocityTemplatingEngine velocityTemplatingEngine;
    @ComponentImport
    private VisibilityValidator visibilityValidator;
    @ComponentImport
    private XsrfTokenGenerator xsrfTokenGenerator;
    @ComponentImport
    private RendererManager rendererManager;
    @ComponentImport
    private UserUtil userUtil;
    @ComponentImport
    private UserProjectHistoryManager userProjectHistoryManager;
    @ComponentImport
    private WorkflowManager workflowManager;
    @ComponentImport
    private WorklogService worklogService;
    @ComponentImport
    private WebFragmentHelper webFragmentHelper;

    @Override
    public String getName() {
        return "Extra work tracking";
    }
}
