package com.scn.jira.automation.impl.domain.service;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@ExportAsService(ScnBIService.class)
public class ScnBIServiceImpl implements ScnBIService {
    private static final Logger LOGGER = Logger.getLogger(ScnBIServiceImpl.class);
    private final static String DRIVER_NAME = "net.sourceforge.jtds.jdbc.Driver";
    private final static String CONNECTION = "jdbc:jtds:sqlserver://SRV-BI:1433;DatabaseName=Jira_DWH;domain=MAIN";
    private final static String LOGIN = "sps-training-admin";
    private final static String PASSWORD = "06#$XPvf";
    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<Date, DayType> getUserCalendar(String userKey, Date from, Date to) {
        Map<Date, DayType> result = new HashMap<>();
        try {
            Class.forName(DRIVER_NAME);
            Connection con = DriverManager.getConnection(CONNECTION, LOGIN, PASSWORD);
            try {
                PreparedStatement pstmt = con.prepareStatement("{call dbo.GetCalendar(?,?,?)}");
                pstmt.setString(1, userKey);
                pstmt.setString(2, from.toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER));
                pstmt.setString(3, to.toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    result.put(
                        Date.from(rs.getDate("Date")
                            .toLocalDate()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()),
                        DayType.getByIndex(rs.getInt("StatusId")));
                }
                rs.close();
                pstmt.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }
}
