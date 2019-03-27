package com.scn.jira.worklog.wl;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.atlassian.jira.component.ComponentAccessor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ExtendedWorklogService {
	private static final Logger LOGGER = Logger.getLogger(ExtendedWorklogService.class);

	private final ExtendedWorklogManager extWorklogManager;
	private final DateTimeFormatterFactory fFactory;
	private final IScnProjectSettingsManager scnProjectSettingsManager;

	@Inject
	public ExtendedWorklogService(ExtendedWorklogManager extendedWorklogManager,
			IScnProjectSettingsManager scnProjectSettingsManager) {
		this.extWorklogManager = extendedWorklogManager;
		this.fFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);;
		this.scnProjectSettingsManager = scnProjectSettingsManager;
	}

	public List<GenericValue> getExtWorklogsByType(JiraServiceContext jiraServiceContext, String worklogTypeId) {
		List<GenericValue> result = null;
		try {
			result = this.extWorklogManager.getExtWorklogsByType(worklogTypeId);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public GenericValue getExtWorklogById(JiraServiceContext jiraServiceContext, Long id) {
		GenericValue gv = null;
		try {
			gv = this.extWorklogManager.getExtWorklog(id);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return gv;
	}

	public void updateWorklogType(JiraServiceContext jiraServiceContext, Long worklogId, String worklogType) {
		try {
			this.extWorklogManager.updateExtWorklogType(worklogId, worklogType);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
	}

	public boolean deleteWorklogType(JiraServiceContext jiraServiceContext, Long worklogId) {
		boolean result = false;
		try {
			result = this.extWorklogManager.deleteExtWorklogType(worklogId);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public Worklog createWorklogType(JiraServiceContext jiraServiceContext, Worklog worklog, String worklogTypeId) {
		Worklog result = null;
		try {
			result = this.extWorklogManager.createExtWorklogType(worklog, worklogTypeId);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public boolean isDateExpired(JiraServiceContext jiraServiceContext, Date startDate, Project project, boolean isDeleteEvent) {
		Date wlBlockingDate = scnProjectSettingsManager.getWLBlockingDate(project.getId());
		long wlBlockingDateInMinutes = (long) Math.floor((wlBlockingDate != null ? wlBlockingDate.getTime() : 0L) / 60000L);
		long startDateInMinutes = (long) Math.floor(startDate.getTime() / 60000L);
		if (wlBlockingDateInMinutes - startDateInMinutes >= 0L) {
			Locale locale = jiraServiceContext.getI18nBean().getLocale();
			String blockingDate = this.fFactory.formatter().withLocale(locale).format(wlBlockingDate);
			String msgKey = isDeleteEvent ? "scn.scnworklog.service.error.blocking.date.on.delete"
					: "scn.scnworklog.service.error.blocking.date.on.save";
			String msgText = getText(jiraServiceContext, msgKey, blockingDate);
			jiraServiceContext.getErrorCollection().addErrorMessage(msgText);
			return true;
		}
		return false;
	}

	private String getText(JiraServiceContext jiraServiceContext, String key) {
		return jiraServiceContext.getI18nBean().getText(key);
	}

	private String getText(JiraServiceContext jiraServiceContext, String key, String param) {
		return jiraServiceContext.getI18nBean().getText(key, param);
	}
}