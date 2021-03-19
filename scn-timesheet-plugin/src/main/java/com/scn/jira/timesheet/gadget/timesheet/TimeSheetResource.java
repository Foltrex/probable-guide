package com.scn.jira.timesheet.gadget.timesheet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverter;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverterImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.DateFieldFormatImpl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import com.scn.jira.timesheet.gadget.rest.error.ErrorCollection;
import com.scn.jira.timesheet.report.timesheet.TimeSheet;
import com.scn.jira.timesheet.report.timesheet.TimeSheetDto;
import com.scn.jira.timesheet.util.CalendarUtil;
import com.scn.jira.timesheet.util.ServletUtil;
import com.scn.jira.timesheet.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.VelocityException;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Named
@Path("/timesheet")
@RequiredArgsConstructor
public class TimeSheetResource {
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final DateTimeFormatterFactory fFactory;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final TimeSheet timeSheet;
    private final UserManager userManager;

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTimesheet(@Context HttpServletRequest request, @QueryParam("targetUser") String targetUserName) {
        int numOfWeeks = ServletUtil.getIntParam(request, "numOfWeeks", 1);

        int reportingDay = ServletUtil.getIntParam(request, "reportingDay", 2);

        ApplicationUser targetUser = this.authenticationContext.getLoggedInUser();
        if ((targetUserName != null) && (targetUserName.length() != 0)) {
            targetUser = userManager.getUserByName(targetUserName);
        }

        VelocityManager vm = ComponentAccessor.getVelocityManager();
        try {
            return Response
                .ok(new TimeSheetRepresentation(vm.getBody("templates/scn/timesheetportlet/",
                    "timesheet-portlet.vm",
                    getVelocityParams(request, numOfWeeks, reportingDay, targetUser))))
                .cacheControl(getNoCacheControl()).build();
        } catch (VelocityException e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    private Map<String, Object> getVelocityParams(HttpServletRequest request, int numOfWeeks, int reportingDay,
                                                  ApplicationUser targetUser) {
        Map<String, Object> params = getVelocityParams(numOfWeeks, reportingDay, targetUser);
        params.put("i18n", this.authenticationContext.getI18nHelper());
        params.put("textutils", new TextUtils());
        params.put("req", request);
        VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(
            this.applicationProperties).getJiraVelocityRequestContext();
        params.put("baseurl", velocityRequestContext.getBaseUrl());
        params.put("requestContext", velocityRequestContext);
        return params;
    }

    private Map<String, Object> getVelocityParams(int numOfWeeks, int reportingDay, ApplicationUser targetUser) {
        Map<String, Object> params = new HashMap<>();
        ApplicationUser user = this.authenticationContext.getLoggedInUser();

        params.put("loggedin", user != null);

        if (user == null) {
            return params;
        }

        I18nBean i18nBean = new I18nBean(user);

        Calendar[] dates = CalendarUtil.getDatesRange(reportingDay, numOfWeeks);
        Calendar startDate = dates[0];
        Calendar endDate = dates[1];
        try {
            params.put("targetUser", targetUser);

            TimeSheetDto timeSheetDto = timeSheet.getTimeSpents(user, startDate.getTime(), endDate.getTime(), targetUser.getKey(), false, null, null,
                null, null, null, null, null, null);

            params.put("weekDays", timeSheetDto.getWeekDays());
            params.put("weekWorkLog", timeSheetDto.getWeekWorkLogShort());
            params.put("weekTotalTimeSpents", timeSheetDto.getWeekTotalTimeSpents());
            params.put("fieldVisibility", this.fieldVisibilityManager);
            DatePickerConverter dpc = new DatePickerConverterImpl(this.authenticationContext,
                new DateFieldFormatImpl(this.fFactory));
            params.put("dpc", dpc);
            params.put("startDate", startDate.getTime());
            endDate.add(Calendar.DAY_OF_YEAR, -1);

            params.put("endDate", endDate.getTime());
            params.put("textUtil", new TextUtil(i18nBean));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    private CacheControl getNoCacheControl() {
        CacheControl noCache = new CacheControl();
        noCache.setNoCache(true);
        return noCache;
    }

    @GET
    @Path("/validate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validate(@QueryParam("num_weeks") String num_weeks, @QueryParam("user") String user,
                             @QueryParam("reporting_day") String reporting_day) {
        ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder();
        if (StringUtils.isBlank(num_weeks)) {
            errorBuilder.addError("num_weeks", "scn.gadget.error.num_weeks.empty", new String[0]);
        }
        ErrorCollection errorCollection = errorBuilder.build();
        if (!errorCollection.hasAnyErrors()) {
            return Response.ok().cacheControl(getNoCacheControl()).build();
        }
        return Response.status(400).entity(errorCollection).cacheControl(getNoCacheControl()).build();
    }
}
