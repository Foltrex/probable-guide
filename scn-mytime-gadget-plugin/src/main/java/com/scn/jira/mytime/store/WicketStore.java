package com.scn.jira.mytime.store;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.scn.jira.mytime.util.DateUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class WicketStore {
    protected static Logger logger = Logger.getLogger(WicketStore.class);
    private final I18nHelper i18nResolver;
    private static String driverName;
    private static String connection;
    private static String loginTatsi;
    private static String password;

    @Inject
    public WicketStore(JiraAuthenticationContext jiraAuthenticationContext) {
        this.i18nResolver = jiraAuthenticationContext.getI18nHelper();
    }

    private void getDbProperties() {
        driverName = i18nResolver.getText("mytime.gadget.driverName");
        connection = i18nResolver.getText("mytime.gadget.connection");
        loginTatsi = i18nResolver.getText("mytime.gadget.login");
        password = i18nResolver.getText("mytime.gadget.password");
    }

    public Map<String, Long> gerUserWicketTimeForthePeriod(String login, Date startDate, Date endDate) {
        getDbProperties();
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

            return timesMap;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return timesMap;
    }
}
