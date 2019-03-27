package com.scn.jira.worklog.types.translation;

//import com.atlassian.jira.config.properties.ApplicationProperties;
//import com.atlassian.jira.issue.IssueConstant;
//import com.atlassian.jira.security.JiraAuthenticationContext;
//import com.atlassian.jira.util.I18nHelper;
//import com.atlassian.jira.web.action.admin.translation.TranslationManager;
//import com.atlassian.jira.web.action.admin.translation.TranslationManagerImpl;
//import com.ibm.bsf.util.Bean;
//import com.opensymphony.module.propertyset.PropertySet;
//import com.opensymphony.util.TextUtils;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.apache.commons.lang.StringUtils;

//import org.springframework.stereotype.Component;

// TODO investigate using of this class. The class overrides system bean and it can behave itself unexpected.
//@Component
public class ExtendedTranslationManagerImpl
//		extends TranslationManagerImpl
//		implements TranslationManager, ExtendedTranslationManager
{

//	private Map<String, ITranslationPrefixesProvider> translationPrefixesProvider;
//	private Map<String, String> translationPrefixMap;
//	private JiraAuthenticationContext authenticationContext;
//	
//	public ExtendedTranslationManagerImpl(
//			com.atlassian.jira.security.JiraAuthenticationContext authenticationContext, com.atlassian.jira.config.properties.ApplicationProperties applicationProperties, com.atlassian.jira.util.I18nHelper.BeanFactory beanFactory
//			//JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, I18nHelper.BeanFactory beanFactory
//			)
//	{
//		super(authenticationContext, applicationProperties, beanFactory);
//		this.authenticationContext = authenticationContext;
//	}
//	
//	public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale, I18nHelper i18n)
//	{
//		PropertySet ps = issueConstant.getPropertySet();
//		
//		String issueConstantType = issueConstant.getGenericValue().getEntityName();
//		String translationPrefix = getTranslationPrefix(issueConstantType);
//		
//		String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);
//		
//		if (!TextUtils.stringSet(translationString))
//		{
//			if (TextUtils.stringSet(translationPrefix))
//			{
//				String propKey = translationPrefix + "." + makeNameIntoProperty(issueConstant.getName()) + "."
//						+ ((name) ? "name" : "desc");
//				
//				if (i18n == null)
//					translationString = this.authenticationContext.getI18nHelper().getText(propKey);
//				else
//				{
//					translationString = i18n.getText(propKey);
//				}
//				
//				if ((translationString != null) && (translationString.indexOf(translationPrefix) == -1))
//				{
//					return translationString;
//				}
//				
//			}
//			
//			if (name)
//			{
//				return issueConstant.getName();
//			}
//			return issueConstant.getDescription();
//		}
//		if (name)
//		{
//			return getTranslatedNameFromString(translationString);
//		}
//		return getTranslatedDescriptionFromString(translationString);
//	}
//	
//	public boolean hasLocaleTranslation(IssueConstant issueConstant, String locale)
//	{
//		PropertySet ps = issueConstant.getPropertySet();
//		
//		String issueConstantType = issueConstant.getGenericValue().getEntityName();
//		String translationPrefix = getTranslationPrefix(issueConstantType);
//		
//		String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);
//		
//		return TextUtils.stringSet(translationString);
//	}
//	
//	private String makeNameIntoProperty(String issueConstantName)
//	{
//		return StringUtils.deleteWhitespace(issueConstantName).toLowerCase();
//	}
//	
//	public String getTranslationPrefix(String issueConstantType)
//	{
//		return (String) getTranslationPrefixMap().get(issueConstantType);
//	}
//	
//	public Map<String, String> getTranslationPrefixMap()
//	{
//		Iterator<Map.Entry<String, ITranslationPrefixesProvider>> iterator;
//		if (this.translationPrefixMap == null)
//		{
//			this.translationPrefixMap = new HashMap<String, String>();
//			for (iterator = getTranslationPrefixesProvider().entrySet().iterator(); iterator.hasNext();)
//			{
//				Map.Entry<String, ITranslationPrefixesProvider> entry = (Map.Entry<String, ITranslationPrefixesProvider>) iterator.next();
//				ITranslationPrefixesProvider translationPrefixesProvider = (ITranslationPrefixesProvider) entry.getValue();
//				this.translationPrefixMap.putAll(translationPrefixesProvider.getTranslationPrefixes());
//			}
//		}
//		return this.translationPrefixMap;
//	}
//	
//	public Map<String, ITranslationPrefixesProvider> getTranslationPrefixesProvider()
//	{
//		if (this.translationPrefixesProvider == null)
//		{
//			this.translationPrefixesProvider = new HashMap<String, ITranslationPrefixesProvider>();
//		}
//		return this.translationPrefixesProvider;
//	}
//	
//	public void addTranslationPrefixesProvider(ITranslationPrefixesProvider _translationPrefixesProvider)
//	{
//		getTranslationPrefixesProvider().put(_translationPrefixesProvider.getClass().getName(), _translationPrefixesProvider);
//	}
}