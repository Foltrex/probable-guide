package com.scn.jira.worklog.scnwl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

@Component
public class ScnIssueEventListener implements InitializingBean, DisposableBean {
    private static final Date BLOCKING_DATE = new Date(1577826000000L);
    public final EventPublisher eventPublisher;
    public final IScnWorklogStore worklogStore;
    private final IScnProjectSettingsManager projectSettingManager;

    @Inject
    public ScnIssueEventListener(EventPublisher eventPublisher, IScnWorklogStore worklogStore,
                                 IScnProjectSettingsManager projectSettingManager) {
        this.eventPublisher = eventPublisher;
        this.worklogStore = worklogStore;
        this.projectSettingManager = projectSettingManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent event) {
        Long eventTypeId = event.getEventTypeId();

        if (EventType.ISSUE_DELETED_ID.equals(eventTypeId)) {
            worklogStore.deleteAllByIssueId(event.getIssue().getId());
        }
    }

    @EventListener
    public void onProjectCreatedEvent(ProjectCreatedEvent event) {
        Long projectId = event.getProject().getId();
        projectSettingManager.setWLTypeRequired(projectId, true);
        projectSettingManager.setWLBlockingDate(projectId, BLOCKING_DATE);
    }
}
