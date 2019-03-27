package com.scn.jira.worklog.core.settings;

import java.util.Date;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;

public interface IScnUserBlockingManager
{	
	public Date getBlockingDate(User user);

	public void setBlockingDate(User user, Date date);
	
	public void setAll(Map<String, Date> properties);
	
	public String getSettingsUrl();
	
	public void setSettingsUrl(String url);
	
	public long getRepeatInterval();
	
	public void setRepeatInterval(long repeatInterval);
	
	public String getDatePattern();
	
	public void setDatePattern(String datePattern);
	
	public Date parse(String dateString);
}
