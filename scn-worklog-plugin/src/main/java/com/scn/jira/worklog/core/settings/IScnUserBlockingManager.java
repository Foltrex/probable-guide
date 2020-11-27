package com.scn.jira.worklog.core.settings;

import java.util.Date;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;

public interface IScnUserBlockingManager {
    Date getBlockingDate(User user);

    void setBlockingDate(User user, Date date);

    void setAll(Map<String, Date> properties);

    String getSettingsUrl();

    void setSettingsUrl(String url);

    long getRepeatInterval();

    void setRepeatInterval(long repeatInterval);

    String getDatePattern();

    void setDatePattern(String datePattern);

    Date parse(String dateString);
}
