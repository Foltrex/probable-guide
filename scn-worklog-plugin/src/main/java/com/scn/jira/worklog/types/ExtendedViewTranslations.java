package com.scn.jira.worklog.types;

import java.util.Collection;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeSchemeService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.action.admin.translation.ViewTranslations;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.types.translation.ExtendedTranslationManager;
import com.scn.jira.worklog.types.translation.ScnViewTranslations;


public class ExtendedViewTranslations extends ViewTranslations {
	private static final long serialVersionUID = 3592917550021702454L;

	protected Collection<IssueConstant> issueConstants;
	protected ExtendedConstantsManager extendedConstantsManager;
	protected ExtendedTranslationManager extendedTranslationManager;
	protected ScnViewTranslations scnViewTranslations;
	protected BeanFactory beanFactory;

	public ExtendedViewTranslations(FieldConfigSchemeManager configSchemeManager, IssueTypeSchemeManager issueTypeSchemeManager,
			FieldManager fieldManager, OptionSetManager optionSetManager, SubTaskManager subTaskManager,
			ApplicationProperties applicationProperties, SearchProvider searchProvider, TranslationManager translationManager,
			ConstantsManager constantsManager, JiraAuthenticationContext authenticationContext, IssueManager issueManager,
			LocaleManager localeManager, ExtendedConstantsManager extendedConstantsManager,
			IssueEventBundleFactory issueEventBundleFactory, BeanFactory beanFactory, BulkMoveOperation bulkMoveOperation, IssueTypeSchemeService issueTypeSchemeService) {
		super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, new IssueTypeManageableOption(
				constantsManager, subTaskManager, applicationProperties, authenticationContext), bulkMoveOperation,
		// new BulkMoveOperationImpl(
		// ComponentAccessor.getWorkflowManager(), ComponentAccessor.getProjectManager(), fieldManager,
		// ComponentAccessor.getIssueFactory(), issueManager, ComponentAccessor.getIssueEventManager(),
		// new BulkEditBeanSessionHelper(), ComponentAccessor.getAttachmentManager(), issueEventBundleFactory),
				translationManager, constantsManager, authenticationContext, issueManager, localeManager, issueTypeSchemeService);

		this.extendedConstantsManager = extendedConstantsManager;
		this.beanFactory = beanFactory;
		this.extendedTranslationManager = null;//new ExtendedTranslationManagerImpl(authenticationContext, applicationProperties,beanFactory);
		this.scnViewTranslations = new ScnViewTranslations(this, extendedConstantsManager, extendedTranslationManager);
	}

	public String getIssueConstantName() {
		String issueConstantName = super.getIssueConstantName();
		issueConstantName = this.scnViewTranslations.getIssueConstantName(issueConstantName);
		return issueConstantName;
	}

	public String getIssueConstantTranslationPrefix() {
		String issueConstantTranslationPrefix = super.getIssueConstantTranslationPrefix();
		issueConstantTranslationPrefix = this.scnViewTranslations
				.getIssueConstantTranslationPrefix(issueConstantTranslationPrefix);
		return issueConstantTranslationPrefix;
	}

	public String getRedirectPage() {
		String redirectPage = super.getRedirectPage();
		redirectPage = this.scnViewTranslations.getRedirectPage(redirectPage);
		return redirectPage;
	}

	public Collection getIssueConstants() {
		if (this.issueConstants == null) {
			this.issueConstants = super.getIssueConstants();
			this.issueConstants = this.scnViewTranslations.getIssueConstants(this.issueConstants);
		}
		return this.issueConstants;
	}

	public String getLinkName() {
		String linkName = super.getLinkName();
		linkName = this.scnViewTranslations.getLinkName(linkName);
		return linkName;
	}
}