package com.scn.jira.util;

import java.util.Calendar;
import java.util.Date;

public class WeekPortletHeader
{
  private Date weekDayDate;
  private String weekDayCSS;

  public WeekPortletHeader()
  {
  }

  public WeekPortletHeader(Date date)
  {
    this.weekDayDate = date;
  }

  public Date getWeekDayDate() {
    return this.weekDayDate;
  }

  public void setWeekDayDate(Date aWeekDayDate) {
    this.weekDayDate = aWeekDayDate;
  }

  public String getWeekDayCSS() {
    return this.weekDayCSS;
  }

  public void setWeekDayCSS(String aWeekDayCSS) {
    this.weekDayCSS = aWeekDayCSS;
  }

  public boolean isBusinessDay() {
    return !isNonBusinessDay();
  }

  public boolean isNonBusinessDay() {
    Calendar calendarHeaderDate = Calendar.getInstance();
    calendarHeaderDate.setTime(this.weekDayDate);
    int dayOfWeek = calendarHeaderDate.get(7);
    return (dayOfWeek == 7) || (dayOfWeek == 1);
  }
}