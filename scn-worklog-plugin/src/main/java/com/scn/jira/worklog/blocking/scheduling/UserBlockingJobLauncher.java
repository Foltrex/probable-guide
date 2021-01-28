package com.scn.jira.worklog.blocking.scheduling;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;

@ExportAsService({UserBlockingJobLauncher.class})
@Named("userBlockingJobLauncher")
public class UserBlockingJobLauncher implements LifecycleAware {
    private static final String JOB_KEY = UserBlockingJobLauncher.class.getName() + ":job";

    private final PluginScheduler pluginScheduler;
    private final IScnUserBlockingManager userBlockingManager;

    @Inject
    public UserBlockingJobLauncher(
        PluginScheduler pluginScheduler,
        IScnUserBlockingManager userBlockingManager) {
        this.pluginScheduler = pluginScheduler;
        this.userBlockingManager = userBlockingManager;
    }

    public void onStart() {
        schedule();
    }

    public void schedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        pluginScheduler.scheduleJob(JOB_KEY, UserBlockingUpdateJob.class, null, calendar.getTime(), userBlockingManager.getRepeatInterval());
    }

    public void onStop() {

    }
}
