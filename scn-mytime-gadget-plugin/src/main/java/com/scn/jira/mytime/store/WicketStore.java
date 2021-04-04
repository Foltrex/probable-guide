package com.scn.jira.mytime.store;

import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.mytime.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import javax.inject.Named;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
@Log4j
@RequiredArgsConstructor
public class WicketStore {
    private static final String driverName = "net.sourceforge.jtds.jdbc.Driver";
    private static final String connection = "jdbc:jtds:sqlserver://SRV-BI:1433;DatabaseName=Jira_DWH;domain=MAIN";
    private static final String loginTatsi = "sps-training-admin";
    private static final String password = "06#$XPvf";
    private final UserManager userManager;

    public Map<String, Long> gerUserWicketTimeForthePeriod(String userKey, Date startDate, Date endDate) {
        Map<String, Long> timesMap = new HashMap<>();

        try {
            Class.forName(driverName);
            Connection con = DriverManager.getConnection((connection), loginTatsi, password);
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
