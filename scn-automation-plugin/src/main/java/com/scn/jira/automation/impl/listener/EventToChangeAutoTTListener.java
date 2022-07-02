package com.scn.jira.automation.impl.listener;

import com.atlassian.crowd.event.user.UserEditedEvent;
import com.atlassian.crowd.event.user.UsersDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class EventToChangeAutoTTListener implements InitializingBean, DisposableBean {
    public final EventPublisher eventPublisher;
    public final AutoTTService autoTTService;
    public final UserManager userManager;

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
            autoTTService.removeAllByIssueId(issueId);
        }
    }

    @EventListener
    public void onProjectEvent(@Nonnull ProjectDeletedEvent event) {
        Long projectId = event.getId();
        autoTTService.removeAllByProjectId(projectId);
    }

    @EventListener
    public void onUserEvent(@Nonnull UsersDeletedEvent event) {
        Collection<String> usernames = event.getUsernames();
        autoTTService.removeAllByUsernames(usernames);
    }

    @EventListener
    public void onUserEvent(@Nonnull UserEditedEvent event) {
        ApplicationUser user = userManager.getUserByName(event.getUser().getName());
        if (user != null) {
            autoTTService.updateByUserKey(user.getKey());
        }
    }
}
