package com.scn.jira.worklog.config;

import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.worklog.api.WorklogPluginComponent;
import com.scn.jira.worklog.blocking.scheduling.UserBlockingJobLauncher;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.IScnTimeTrackingIssueManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.scnwl.ScnTimeTrackingIssueManager;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.listener.WorklogPluginLauncher;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import com.scn.jira.worklog.settings.IScnProjectSettingsService;
import com.scn.jira.worklog.settings.ScnProjectSettingsService;
import com.scn.jira.worklog.wl.OverridedWorklogManager;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;

@Configuration
public class WorklogPluginOsgiServiceConfig {
    @Bean
    public FactoryBean<ServiceRegistration> registerWorklogPluginComponent(final WorklogPluginComponent worklogPluginComponent) {
        return exportOsgiService(worklogPluginComponent, null, WorklogPluginComponent.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerExtendedWorklogManager(final ExtendedWorklogManager extendedWorklogManager) {
        return exportOsgiService(extendedWorklogManager, null, ExtendedWorklogManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerOverridedWorklogManager(final OverridedWorklogManager overridedWorklogManager) {
        return exportOsgiService(overridedWorklogManager, null, WorklogManager.class, OverridedWorklogManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnUserBlockingManager(final IScnUserBlockingManager scnUserBlockingManager) {
        return exportOsgiService(scnUserBlockingManager, null, IScnUserBlockingManager.class, ScnUserBlockingManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnProjectSettingsService(final IScnProjectSettingsService scnProjectSettingsService) {
        return exportOsgiService(scnProjectSettingsService, null, IScnProjectSettingsService.class, ScnProjectSettingsService.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnWorklogStore(final IScnWorklogStore scnWorklogStore) {
        return exportOsgiService(scnWorklogStore, null, IScnWorklogStore.class, OfBizScnWorklogStore.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnWorklogManager(final IScnWorklogManager scnWorklogManager) {
        return exportOsgiService(scnWorklogManager, null, IScnWorklogManager.class, DefaultScnWorklogManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIGlobalSettingsManager(final IGlobalSettingsManager globalSettingsManager) {
        return exportOsgiService(globalSettingsManager, null, IGlobalSettingsManager.class, GlobalSettingsManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnWorklogService(final IScnWorklogService scnWorklogService) {
        return exportOsgiService(scnWorklogService, null, IScnWorklogService.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerUserBlockingJobLauncher(final UserBlockingJobLauncher userBlockingJobLauncher) {
        return exportOsgiService(userBlockingJobLauncher, null, UserBlockingJobLauncher.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnProjectSettingsManager(final IScnProjectSettingsManager scnProjectSettingsManager) {
        return exportOsgiService(scnProjectSettingsManager, null, IScnProjectSettingsManager.class, ScnProjectSettingsManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnTimeTrackingIssueManager(final IScnTimeTrackingIssueManager scnTimeTrackingIssueManager) {
        return exportOsgiService(scnTimeTrackingIssueManager, null, IScnTimeTrackingIssueManager.class, ScnTimeTrackingIssueManager.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerPluginLauncher(final WorklogPluginLauncher pluginLauncher) {
        return exportOsgiService(pluginLauncher, null, LifecycleAware.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerIScnExtendedIssueStore(final IScnExtendedIssueStore scnExtendedIssueStore) {
        return exportOsgiService(scnExtendedIssueStore, null, IScnExtendedIssueStore.class, OfBizScnExtendedIssueStore.class);
    }

    @Bean
    public FactoryBean<ServiceRegistration> registerExtendedConstantsManager(final ExtendedConstantsManager extendedConstantsManager) {
        return exportOsgiService(extendedConstantsManager, null, ExtendedConstantsManager.class, DefaultExtendedConstantsManager.class);
    }
}
