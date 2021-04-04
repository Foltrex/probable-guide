package com.scn.jira.logtime.manager;

import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.logtime.representation.WicketRepresentation;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import javax.inject.Named;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
@Log4j
@RequiredArgsConstructor
public class WicketManager {
    private final static String driverName1 = "net.sourceforge.jtds.jdbc.Driver";
    private final static String connection1 = "jdbc:jtds:sqlserver://SRV-BI:1433;DatabaseName=Jira_DWH;domain=MAIN";
    private final static String loginTatsi1 = "sps-training-admin";
    private final static String password1 = "06#$XPvf";

    private final UserManager userManager;

    public WicketRepresentation gerUserWicketTimeForthePeriod(String login, Date startDate, Date endDate) {
        ArrayList<String> times = new ArrayList<>();
        Map<String, Long> timesMap = new HashMap<>();

        List<String> datesForList = DateUtils.getDatesList(startDate, endDate, DateUtils.formatStringDay);
        WicketRepresentation wicketRepresentation;

        try {
            Class.forName(driverName1);
            Connection con = DriverManager.getConnection(connection1, loginTatsi1, password1);
            Statement s = con.createStatement();

            List<String> dates = DateUtils.getDatesList(startDate, endDate, DateUtils.formatStringDateDb);

            String startDay = "";
            String endDay = "";

            if (dates.size() != 0) {
                startDay = dates.get(0);
                endDay = dates.get(dates.size() - 1);
            }

            String sql = "SELECT TOP 100 [ID], [Person],[Office_time],[Day] FROM [Jira_DWH].[dbo].[WorkDays]"
                + " where Person ='" + login + "' and Day >='" + startDay + "' and Day<='" + endDay + "'";

            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                long office_time = rs.getLong("Office_time");
                String day = rs.getString("Day");

                timesMap.put(day, office_time);
            }

            rs.close();
            s.close();
            con.close();

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            long total = 0;
            for (String dateFor : datesForList) {
                if (timesMap.get(dateFor) != null) {
                    times.add(TextFormatUtil.timeToString(timesMap.get(dateFor)));
                    total = total + timesMap.get(dateFor);
                } else {
                    times.add("");
                }
            }
            wicketRepresentation = new WicketRepresentation(login, times, TextFormatUtil.timeToString(total));
        }
        return wicketRepresentation;
    }

    public Map<String, WicketRepresentation> gerUserWicketTimeForthePeriods(List<String> userKeys, Date startDate,
                                                                            Date endDate) {
        Map<String, WicketRepresentation> wicketRepMap = new HashMap<>();

        for (String userKey : userKeys) {
            WicketRepresentation wick = gerUserWicketTimeForthePeriod(Objects.requireNonNull(userManager.getUserByKey(userKey)).getUsername(), startDate, endDate);
            wicketRepMap.put(userKey, wick);
        }

        return wicketRepMap;
    }

    public boolean gerUserWicketPermission(String login) {
        boolean result = false;
        try {
            Class.forName(driverName1);
            Connection con = DriverManager.getConnection(connection1, loginTatsi1, password1);
            Statement s = con.createStatement();

            String sql = "SELECT TOP 1000 [login] FROM [Jira_DWH].[dbo].[wicket_managers]" + " where login ='" + login + "'";

            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                result = true;
            }

            rs.close();
            s.close();
            con.close();

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return result;
    }

    public Map<String, Map<String, Integer>> gerUsersCalendar(List<String> userKeys, Date startDate, Date endDate) {
        Map<String, Map<String, Integer>> usersCalMap = new HashMap<>();

        for (String userKey : userKeys) {
            Map<String, Integer> userCalMap = gerUserCalendar(Objects.requireNonNull(userManager.getUserByKey(userKey)).getUsername(), startDate, endDate);
            usersCalMap.put(userKey, userCalMap);
        }
        return usersCalMap;
    }

    public Map<String, Integer> gerUserCalendar(String login, Date startDate, Date endDate) {
        Map<String, Integer> calendarMap = new HashMap<>();

        String startDay = DateUtils.stringDate(startDate, DateUtils.formatStringDay);
        String endDay = DateUtils.stringDate(endDate, DateUtils.formatStringDay);

        try {
            Class.forName(driverName1);
            Connection con = DriverManager.getConnection(connection1, loginTatsi1, password1);
            try {
                PreparedStatement pstmt = con.prepareStatement("{call dbo.GetCalendar(?,?,?)}");
                pstmt.setString(1, login);
                pstmt.setString(2, startDay);
                pstmt.setString(3, endDay);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    calendarMap.put(rs.getString("Date"), rs.getInt("StatusId"));
                }
                rs.close();
                pstmt.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return calendarMap;
    }
}
