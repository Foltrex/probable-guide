package com.scn.jira.automation.impl.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor
public class IssueEventListener implements InitializingBean, DisposableBean {
    public final EventPublisher eventPublisher;

    @Override
    public void afterPropertiesSet() {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(@Nonnull IssueEvent event) {
        Long eventTypeId = event.getEventTypeId();
        Long issueId = event.getIssue().getId();

        if (EventType.ISSUE_DELETED_ID.equals(eventTypeId)) {
        }
    }
}
