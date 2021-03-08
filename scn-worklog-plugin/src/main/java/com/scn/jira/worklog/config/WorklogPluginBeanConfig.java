package com.scn.jira.worklog.config;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.IssueTypeSchemeService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
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
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
    ModuleFactoryBean.class,
    PluginAccessorBean.class
})
public class WorklogPluginBeanConfig {
    @Bean
    public ActiveObjects activeObjects() {
        return importOsgiService(ActiveObjects.class);
    }

    @Bean
    public AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory() {
        return importOsgiService(AggregateTimeTrackingCalculatorFactory.class);
    }

    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public I18nHelper.BeanFactory beanFactory() {
        return importOsgiService(I18nHelper.BeanFactory.class);
    }

    @Bean
    public CommentService commentService() {
        return importOsgiService(CommentService.class);
    }

    @Bean
    public ConstantsManager constantsManager() {
        return importOsgiService(ConstantsManager.class);
    }

    @Bean
    public DateTimeFormatterFactory dateTimeFormatterFactory() {
        return importOsgiService(DateTimeFormatterFactory.class);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return importOsgiService(EventPublisher.class);
    }

    @Bean
    public FieldConfigSchemeManager fieldConfigSchemeManager() {
        return importOsgiService(FieldConfigSchemeManager.class);
    }

    @Bean
    public FieldLayoutManager fieldLayoutManager() {
        return importOsgiService(FieldLayoutManager.class);
    }

    @Bean
    public FieldManager fieldManager() {
        return importOsgiService(FieldManager.class);
    }

    @Bean
    public FieldVisibilityManager fieldVisibilityManager() {
        return importOsgiService(FieldVisibilityManager.class);
    }

    @Bean
    public FeatureManager featureManager() {
        return importOsgiService(FeatureManager.class);
    }

    @Bean
    public GenericConfigManager genericConfigManager() {
        return importOsgiService(GenericConfigManager.class);
    }

    @Bean
    public GlobalPermissionManager globalPermissionManager() {
        return importOsgiService(GlobalPermissionManager.class);
    }

    @Bean
    public GroupManager groupManager() {
        return importOsgiService(GroupManager.class);
    }

    @Bean
    public IssueManager issueManager() {
        return importOsgiService(IssueManager.class);
    }

    @Bean
    public IssueTypeSchemeManager issueTypeSchemeManager() {
        return importOsgiService(IssueTypeSchemeManager.class);
    }

    @Bean
    public IssueTypeSchemeService issueTypeSchemeService() {
        return importOsgiService(IssueTypeSchemeService.class);
    }

    @Bean
    public IssueEventBundleFactory issueEventBundleFactory() {
        return importOsgiService(IssueEventBundleFactory.class);
    }

    @Bean
    public JiraAuthenticationContext jiraAuthenticationContext() {
        return importOsgiService(JiraAuthenticationContext.class);
    }

    @Bean
    public JqlLocalDateSupport jqlLocalDateSupport() {
        return importOsgiService(JqlLocalDateSupport.class);
    }

    @Bean
    public JqlDateSupport jqlDateSupport() {
        return importOsgiService(JqlDateSupport.class);
    }

    @Bean
    public LocaleManager localeManager() {
        return importOsgiService(LocaleManager.class);
    }

    @Bean
    public OfBizDelegator ofBizDelegator() {
        return importOsgiService(OfBizDelegator.class);
    }

    @Bean
    public OptionSetManager optionSetManager() {
        return importOsgiService(OptionSetManager.class);
    }

    @Bean
    public PermissionManager permissionManager() {
        return importOsgiService(PermissionManager.class);
    }

    @Bean
    public ProjectRoleManager projectRoleManager() {
        return importOsgiService(ProjectRoleManager.class);
    }

    @Bean
    public PluginScheduler pluginScheduler() {
        return importOsgiService(PluginScheduler.class);
    }

    @Bean
    public ProjectManager projectManager() {
        return importOsgiService(ProjectManager.class);
    }

    @Bean
    public TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory() {
        return importOsgiService(TimeTrackingGraphBeanFactory.class);
    }

    @Bean
    public TimeTrackingConfiguration timeTrackingConfiguration() {
        return importOsgiService(TimeTrackingConfiguration.class);
    }

    @Bean
    public TimeTrackingIssueUpdater timeTrackingIssueUpdater() {
        return importOsgiService(TimeTrackingIssueUpdater.class);
    }

    @Bean
    public TranslationManager translationManager() {
        return importOsgiService(TranslationManager.class);
    }

    @Bean
    public SubTaskManager subTaskManager() {
        return importOsgiService(SubTaskManager.class);
    }

    @Bean
    public SearchProvider searchProvider() {
        return importOsgiService(SearchProvider.class);
    }

    @Bean
    public VelocityRequestContextFactory velocityRequestContextFactory() {
        return importOsgiService(VelocityRequestContextFactory.class);
    }

    @Bean
    public VelocityTemplatingEngine velocityTemplatingEngine() {
        return importOsgiService(VelocityTemplatingEngine.class);
    }

    @Bean
    public VisibilityValidator visibilityValidator() {
        return importOsgiService(VisibilityValidator.class);
    }

    @Bean
    public XsrfTokenGenerator xsrfTokenGenerator() {
        return importOsgiService(XsrfTokenGenerator.class);
    }

    @Bean
    public RendererManager rendererManager() {
        return importOsgiService(RendererManager.class);
    }

    @Bean
    public UserUtil userUtil() {
        return importOsgiService(UserUtil.class);
    }

    @Bean
    public UserProjectHistoryManager userProjectHistoryManager() {
        return importOsgiService(UserProjectHistoryManager.class);
    }

    @Bean
    public WorkflowManager workflowManager() {
        return importOsgiService(WorkflowManager.class);
    }

    @Bean
    public WorklogService worklogService() {
        return importOsgiService(WorklogService.class);
    }

    @Bean
    public WebFragmentHelper webFragmentHelper() {
        return importOsgiService(WebFragmentHelper.class);
    }
}
