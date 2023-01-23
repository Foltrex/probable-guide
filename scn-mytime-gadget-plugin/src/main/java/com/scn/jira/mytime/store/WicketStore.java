package com.scn.jira.mytime.store;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.mytime.util.DateUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Named
@Log4j
public class WicketStore implements InitializingBean {
    private final String driverName;
    private final String connection;
    private final String login;
    private final String password;
    private final UserManager userManager;

    public WicketStore(UserManager userManager, JiraHome jiraHome) {
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

    public Map<String, Long> gerUserWicketTimeForthePeriod(String userKey, Date startDate, Date endDate) {
        Map<String, Long> timesMap = new HashMap<>();

        try {
            Class.forName(driverName);
            Connection con = DriverManager.getConnection((connection), login, password);
            Statement s = con.createStatement();

            List<String> dates = DateUtils.getDatesList(startDate, endDate, DateUtils.formatStringDateDb);

            String startDay = "";
            String endDay = "";

            if (dates.size() != 0) {
                startDay = dates.get(0);
                endDay = dates.get(dates.size() - 1);
            }

            String sql = "SELECT TOP 100 [ID], [Person],[Office_time],[Day] FROM [Jira_DWH].[dbo].[WorkDays]"
                + " where Person ='"
                + Objects.requireNonNull(userManager.getUserByKey(userKey)).getUsername()
                + "' and Day >='" + startDay + "' and Day<='" + endDay + "'";

            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                long office_time = rs.getLong("Office_time");
                String day = rs.getString("Day");

                timesMap.put(day, office_time);
            }

            rs.close();
            s.close();
            con.close();

            return timesMap;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return timesMap;
    }
}
