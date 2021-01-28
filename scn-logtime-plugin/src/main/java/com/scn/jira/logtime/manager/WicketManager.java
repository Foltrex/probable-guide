package com.scn.jira.logtime.manager;

import com.scn.jira.logtime.representation.WicketRepresentation;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import org.apache.log4j.Logger;

import javax.inject.Named;
import java.sql.*;
import java.util.Date;
import java.util.*;

@Named
public class WicketManager {
    private static final Logger LOGGER = Logger.getLogger(WicketManager.class);

    private final static String driverName1 = "net.sourceforge.jtds.jdbc.Driver";
    private final static String connection1 = "jdbc:jtds:sqlserver://SRV-BI:1433;DatabaseName=Jira_DWH;domain=MAIN";
    private final static String loginTatsi1 = "sps-training-admin";
    private final static String password1 = "06#$XPvf";

    public WicketRepresentation gerUserWicketTimeForthePeriod(String login, Date startDate, Date endDate) {
        ArrayList<String> times = new ArrayList<>();
        Map<String, Long> timesMap = new HashMap<>();

        List<String> datesForList = DateUtils.getDatesList(startDate, endDate, DateUtils.formatStringDay);
        WicketRepresentation wicketRepresentation = new WicketRepresentation();

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
            LOGGER.error(e.getMessage());
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

    public Map<String, WicketRepresentation> gerUserWicketTimeForthePeriods(List<String> logins, Date startDate,
                                                                            Date endDate) {
        Map<String, WicketRepresentation> wicketRepMap = new HashMap<>();

        for (String login : logins) {
            login = login.trim();
            WicketRepresentation wick = gerUserWicketTimeForthePeriod(login, startDate, endDate);
            wicketRepMap.put(login, wick);
        }

        return wicketRepMap;
    }

    public boolean gerUserWicketPermission(String login) {
        boolean result = false;
        try {
            Class.forName(driverName1);
            Connection con = DriverManager.getConnection(connection1, loginTatsi1, password1);
            Statement s = con.createStatement();

            String sql = "SELECT TOP 1000 [login] FROM [Jira_DWH].[dbo].[wicket_managers]" + " where login ='" + login
                + "'";

            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                //String person = rs.getString("login");
                result = true;
            }

            rs.close();
            s.close();
            con.close();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    public Map<String, Map<String, Integer>> gerUsersCalendar(List<String> logins, Date startDate, Date endDate) {
        Map<String, Map<String, Integer>> usersCalMap = new HashMap<>();

        for (String login : logins) {
            login = login.trim();
            Map<String, Integer> userCalMap = gerUserCalendar(login, startDate, endDate);
            usersCalMap.put(login, userCalMap);
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
                LOGGER.error(e.getMessage());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return calendarMap;
    }
}
