package com.scn.jira.worklog.scnwl;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ScnIssueEventListener implements InitializingBean, DisposableBean
{	
	public final EventPublisher eventPublisher;
	public final IScnWorklogStore worklogStore;

	@Inject
	public ScnIssueEventListener(EventPublisher eventPublisher, IScnWorklogStore worklogStore) {
		this.eventPublisher = eventPublisher;
		this.worklogStore = worklogStore;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		eventPublisher.register(this);
	}
	
	@Override
	public void destroy() throws Exception
	{
		eventPublisher.unregister(this);
	}

	@EventListener
	public void onIssueEvent(IssueEvent event)
	{
		Long eventTypeId = event.getEventTypeId();
		
		if (EventType.ISSUE_DELETED_ID.equals(eventTypeId)) 
		{
			worklogStore.deleteAllByIssueId(event.getIssue().getId());
		}
	}
}
