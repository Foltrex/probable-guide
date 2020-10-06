package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

@PublicApi
public interface ScnBIService {
    Map<Date, DayType> getUserCalendar(String userKey, Date from, Date to);

    enum DayType {
        WORKING(1),
        WEEKEND(2),
        HOLIDAY(3);

        private final int index;

        DayType(int index) {
            this.index = index;
        }

        @Nonnull
        public static DayType getByIndex(int index) {
            for (DayType value : values()) {
                if (value.index == index) {
                    return value;
                }
            }
            throw new IllegalArgumentException("There is no such index for DayType: " + index);
        }
    }
}
