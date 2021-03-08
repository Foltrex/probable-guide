package com.scn.jira.automation.impl.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseResource {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    protected Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }
}
