package com.scn.jira.automation.impl.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.AbstractService;
import com.opensymphony.module.propertyset.PropertySet;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class AutoTTExecutionService extends AbstractService {
    private static final Logger LOGGER = Logger.getLogger(AutoTTExecutionService.class);
    public static final String DEFAULT_NAME = "Auto time tracking";
    public static final String DEFAULT_CRON_SCHEDULE = "0 0 * * * ?";
    public static final Long WORKED_TIME = 28800L;

    private AutoTTService autoTTService;
    private ScnBIService scnBIService;
    private WorklogContextService worklogContextService;

    @Override
    public void init(PropertySet props, long configurationIdentifier) throws ObjectConfigurationException {
        autoTTService = ComponentAccessor.getOSGiComponentInstanceOfType(AutoTTService.class);
        scnBIService = ComponentAccessor.getOSGiComponentInstanceOfType(ScnBIService.class);
        worklogContextService = ComponentAccessor.getOSGiComponentInstanceOfType(WorklogContextService.class);
    }

    @Override
    public void run() {
        Date from = Date.from(LocalDate.now().minusDays(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
            to = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        autoTTService.getAllActive().forEach(record -> doTimeTracking(record, from, to));
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return this.getObjectConfiguration("AutoTTExecutionService", "services/AutoTTExecutionService.xml", null);
    }

    @Transactional
    private void doTimeTracking(@Nonnull AutoTTDto autoTTDto, Date from, Date to) {
        Map<Date, ScnBIService.DayType> userCalendar = scnBIService.getUserCalendar(autoTTDto.getUser().getKey(), from, to);
        Set<Date> workedDays = worklogContextService.getWorkedDays(autoTTDto.getUser().getKey(), from, to);
        userCalendar.forEach((date, dayType) -> {
            if (dayType.equals(ScnBIService.DayType.WORKING) && !workedDays.contains(date)) {
                worklogContextService.createWorklog(autoTTDto, date);
                LOGGER.debug(autoTTDto + " for date: " + date);
            }
        });
    }
}
