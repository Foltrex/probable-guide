package com.scn.jira.logtime.manager;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.logtime.representation.WicketRepresentation;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
import java.util.Properties;

@Named
@Log4j
public class WicketManager implements InitializingBean {
    private final String driverName;
    private final String connection;
    private final String login;
    private final String password;

    private final UserManager userManager;

    public WicketManager(UserManager userManager, JiraHome jiraHome) {
        this.userManager = userManager;
        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream(new File(jiraHome.getHome(), "scn-bi.properties"))) {
            props.load(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Cannot load scn-bi.properties");
        }
        driverName = props.getProperty("driver-name");
        connection = props.getProperty("connection");
        login = props.getProperty("login");
        password = props.getProperty("password");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class.forName(driverName);
        try (Connection con = DriverManager.getConnection(connection, login, password);
             PreparedStatement ps = con.prepareStatement("select 1")) {
            ps.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Cannot connect to the SCN BI database");
        }
    }

    public WicketRepresentation gerUserWicketTimeForthePeriod(String login, Date startDate, Date endDate) {
        ArrayList<String> times = new ArrayList<>();
        Map<String, Long> timesMap = new HashMap<>();

        List<String> datesForList = DateUtils.getDatesList(startDate, endDate, DateUtils.formatStringDay);
        WicketRepresentation wicketRepresentation;

        try {
            Class.forName(driverName);
            Connection con = DriverManager.getConnection(connection, this.login, password);
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
            Class.forName(driverName);
            Connection con = DriverManager.getConnection(connection, this.login, password);
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
            Class.forName(driverName);
            Connection con = DriverManager.getConnection(connection, this.login, password);
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
