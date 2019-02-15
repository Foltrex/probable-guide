package com.scn.jira.worklog.types.translation;

import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import java.util.Map;

public abstract interface ExtendedTranslationManager extends TranslationManager
{
  public abstract Map<String, ITranslationPrefixesProvider> getTranslationPrefixesProvider();

  public abstract void addTranslationPrefixesProvider(ITranslationPrefixesProvider paramITranslationPrefixesProvider);

  public abstract String getTranslationPrefix(String paramString);
}