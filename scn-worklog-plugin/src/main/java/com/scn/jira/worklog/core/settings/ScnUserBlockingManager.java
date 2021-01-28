package com.scn.jira.worklog.core.settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.opensymphony.module.propertyset.PropertySet;

@ExportAsService({ScnUserBlockingManager.class, IScnUserBlockingManager.class})
@Named("scnUserBlockingManager")
public class ScnUserBlockingManager implements IScnUserBlockingManager {
    private static final String BLOCKING_DATE_FOR = "scn_user_blocking_date_for_";

    private String settingsUrl = "http://localhost:8888/data.xml";
    private long repeatInterval = 24L * 60 * 60 * 1000;
    private String datePattern = "dd-MM-yyyy";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);

    private final PropertiesManager pm;

    public ScnUserBlockingManager() {
        this.pm = ComponentAccessor.getComponent(PropertiesManager.class);
    }

    @Nullable
    public Date getBlockingDate(User user) {
        Assertions.notNull("user", user);

        return pm.getPropertySet().getDate(BLOCKING_DATE_FOR + user.getName());
    }

    public void setBlockingDate(User user, Date date) {
        Assertions.notNull("user", user);

        pm.getPropertySet().setDate(BLOCKING_DATE_FOR + user.getName(), date);
    }

    public void setAll(Map<String, Date> properties) {
        PropertySet ps = pm.getPropertySet();

        @SuppressWarnings("unchecked")
        Collection<String> keys = ps.getKeys(BLOCKING_DATE_FOR, PropertySet.DATE);
        for (String key : keys) {
            ps.remove(key);
        }

        for (Map.Entry<String, Date> property : properties.entrySet()) {
            ps.setDate(BLOCKING_DATE_FOR + property.getKey(), property.getValue());
        }
    }

    public String getSettingsUrl() {
        return this.settingsUrl;
    }

    public void setSettingsUrl(String url) {
        this.settingsUrl = url;
    }

    public long getRepeatInterval() {
        return this.repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * Parses dateString and set up date's time to 23h 59m 59s 990ms
     *
     * @param dateString should match the pattern
     * @return parsed date or null if date doesn't match the pattern
     */
    @Nullable
    public Date parse(String dateString) {
        try {
            Date date = dateFormat.parse(dateString);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 990);

            return cal.getTime();
        } catch (ParseException e) {
            return null;
        }
    }
}
