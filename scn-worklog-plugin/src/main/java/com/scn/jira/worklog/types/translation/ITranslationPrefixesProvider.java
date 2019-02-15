package com.scn.jira.worklog.types.translation;

import java.util.Map;

public abstract interface ITranslationPrefixesProvider
{
  public abstract Map<String, String> getTranslationPrefixes();
}