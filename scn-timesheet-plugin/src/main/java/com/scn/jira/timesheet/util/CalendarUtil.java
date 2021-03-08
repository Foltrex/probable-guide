package com.scn.jira.timesheet.util;

import java.util.Calendar;

public final class CalendarUtil {
    public static Calendar[] getDatesRange(int reportingDay, int numOfWeeks) {
        Calendar currentDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        if (reportingDay != 0)
            endDate.set(7, reportingDay);
        else {
            endDate.add(5, 1);
        }

        endDate.set(11, 0);
        endDate.set(12, 0);
        endDate.set(13, 0);
        endDate.set(14, 0);

        if (endDate.before(currentDate)) {
            endDate.add(4, 1);
        }

        Calendar startDate = Calendar.getInstance();
        startDate.setTime(endDate.getTime());
        startDate.add(3, -numOfWeeks);

        return new Calendar[]{startDate, endDate};
    }
}
