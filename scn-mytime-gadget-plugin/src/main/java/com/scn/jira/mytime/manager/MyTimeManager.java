package com.scn.jira.mytime.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.I18nHelper;
import com.scn.jira.mytime.representation.DayRepresentation;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.mytime.representation.WeekRepresentation;
import com.scn.jira.mytime.store.ScnWorklogMyTimeStore;
import com.scn.jira.mytime.store.WicketStore;
import com.scn.jira.mytime.util.DateUtils;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class MyTimeManager {
    private final ScnWorklogMyTimeStore scnWorklogMyTimeStore;
    private final WicketStore wicketStore;

    @Inject
    public MyTimeManager(ScnWorklogMyTimeStore scnWorklogMyTimeStore, WicketStore wicketStore) {
        this.scnWorklogMyTimeStore = scnWorklogMyTimeStore;
        this.wicketStore = wicketStore;
    }

    public List<WeekRepresentation> getWeeksRepresentation(String user, Date startDate, Date endDate, String reportView) throws DataAccessException {

        List<WeekRepresentation> weeksRepresentations = new ArrayList<WeekRepresentation>();

        List<IScnWorklog> worklogs = scnWorklogMyTimeStore.getScnWorklogsByUserBetweenDates(startDate, endDate, user);

        Map<String, Long> workLogSums = new HashMap<String, Long>();
        for (IScnWorklog worklog : worklogs) {
            String key = DateUtils.stringDate(worklog.getStartDate(), DateUtils.formatStringDay);
            workLogSums.put(key, ((workLogSums.get(key) == null) ? worklog.getTimeSpent() : workLogSums.get(key) + worklog.getTimeSpent()));
        }

        Map<String, Long> wicketTimes = new HashMap<String, Long>();
        wicketTimes = wicketStore.gerUserWicketTimeForthePeriod(user, startDate, endDate);

        for (String wicketTimeKey : wicketTimes.keySet()) {
            System.out.println("key: " + wicketTimeKey + " value: " + wicketTimes.get(wicketTimeKey));
        }

        List<java.util.Date> datesWeek = DateUtils.getDatesListDate(startDate, endDate);
        int etalonMonth = -1;
        if (reportView == "month") {
            Date etalon = datesWeek.get(datesWeek.size() / 2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(etalon);
            etalonMonth = cal.get(Calendar.MONTH);
        }

        int daysCount = 0;
        WeekRepresentation weeksRepresentation = new WeekRepresentation();
        List<DayRepresentation> daysRepresentations = new ArrayList<DayRepresentation>();
        long totalWicket = 0;
        long total = 0;
        for (Date dateWeek : datesWeek) {
            if (daysCount == 0) {
                weeksRepresentation = new WeekRepresentation();
                daysRepresentations = new ArrayList<DayRepresentation>();
                totalWicket = 0;
                total = 0;
            }
            DayRepresentation dayRepresentation = new DayRepresentation();
            String dateString = DateUtils.stringDate(dateWeek, DateUtils.formatStringDay);

            dayRepresentation.setDate(dateWeek);
            dayRepresentation.setDateString(dateString);
            dayRepresentation.setDay(DateUtils.stringDate(dateWeek, DateUtils.formatStringDayNumber));
            if (dateString.equals(DateUtils.stringDate(new Date(), DateUtils.formatStringDay))) {
                dayRepresentation.setDayColor("current");
            } else {
                dayRepresentation.setDayColor("");
            }

            if (etalonMonth != -1) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateWeek);
                if (cal.get(Calendar.MONTH) != etalonMonth) {
                    dayRepresentation.setMonth(false);
                } else {
                    dayRepresentation.setMonth(true);
                }

            } else {
                dayRepresentation.setMonth(true);

            }

            Long time = 0L;
            if (workLogSums.get(dateString) != null) {
                time = workLogSums.get(dateString);
            }
            dayRepresentation.setTime(DateUtils.timeToString(time));
            dayRepresentation.setTimelong(time);
            dayRepresentation.setCssClassWl(DateUtils.getDayColor(time, false));
            Long wTime = 0L;
            if (wicketTimes.get(dateString) != null) {
                wTime = wicketTimes.get(dateString);
            }
            dayRepresentation.setWicketTime(DateUtils.timeToString(wTime));
            dayRepresentation.setWicketTimeLong(wTime);
            dayRepresentation.setCssClassWicket(DateUtils.getDayColor(wTime, false));
            daysRepresentations.add(dayRepresentation);
            totalWicket = totalWicket + wTime.longValue();
            total = total + time.longValue();
            daysCount++;

            if (daysCount == 7) {
                daysCount = 0;
                weeksRepresentation.setDaysRepresentation(daysRepresentations);
                weeksRepresentation.setTotal(DateUtils.timeToString(total));
                weeksRepresentation.setTotalWicket(DateUtils.timeToString(totalWicket));
                weeksRepresentation.setCssClassWlTotal(DateUtils.getDayColor(total, true));
                weeksRepresentation.setCssClassWicketTotal(DateUtils.getDayColor(totalWicket, true));
                weeksRepresentations.add(weeksRepresentation);
            }
        }

        return weeksRepresentations;
    }

    ;

}
