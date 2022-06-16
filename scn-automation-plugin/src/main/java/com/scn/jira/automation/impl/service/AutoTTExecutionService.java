package com.scn.jira.automation.impl.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.opensymphony.module.propertyset.PropertySet;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.automation.impl.domain.repository.AutoTTRepository;
import com.scn.jira.common.exception.InternalRuntimeException;
import lombok.extern.log4j.Log4j;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Log4j
public class AutoTTExecutionService extends AbstractService {
    public static final String DEFAULT_NAME = "Auto time tracking";
    public static final String DEFAULT_CRON_SCHEDULE = "0 0 * * * ?";

    private AutoTTRepository autoTTRepository;
    private ScnBIService scnBIService;
    private WorklogContextService worklogContextService;

    @Override
    public void init(PropertySet props, long configurationIdentifier) throws ObjectConfigurationException {
        autoTTRepository = ComponentAccessor.getOSGiComponentInstanceOfType(AutoTTRepository.class);
        scnBIService = ComponentAccessor.getOSGiComponentInstanceOfType(ScnBIService.class);
        worklogContextService = ComponentAccessor.getOSGiComponentInstanceOfType(WorklogContextService.class);
    }

    @Override
    public void run() {
        LocalDate to = LocalDate.now().minusDays(1);
        autoTTRepository.findAllByActiveTrue().forEach(value -> doTimeTracking(value, to));
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return this.getObjectConfiguration("AutoTTExecutionService", "services/AutoTTExecutionService.xml", null);
    }

    private void doTimeTracking(@Nonnull AutoTT autoTT, LocalDate to) {
        Map<Date, ScnBIService.DayType> userCalendar = scnBIService.getUserCalendar(autoTT.getUsername(), autoTT.getStartDate().toLocalDateTime().toLocalDate(), to);
        Set<Date> workedDays = worklogContextService.getWorkedDays(autoTT.getUserKey(), autoTT.getStartDate().toLocalDateTime().toLocalDate(), to);
        Transaction txn = Txn.begin();
        try {
            userCalendar.forEach((date, dayType) -> {
                if (dayType.equals(ScnBIService.DayType.WORKING) && !workedDays.contains(date)) {
                    worklogContextService.createScnWorklog(autoTT, date);
                }
            });
            autoTT.setStartDate(Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            autoTTRepository.save(autoTT);
            txn.commit();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new InternalRuntimeException(e);
        } finally {
            txn.finallyRollbackIfNotCommitted();
        }
    }
}
