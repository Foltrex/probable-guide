package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.config.util.JiraHome;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.common.exception.InternalRuntimeException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Log4j
public class ScnBIServiceImpl implements ScnBIService, InitializingBean {
    private final String driverName;
    private final String connection;
    private final String login;
    private final String password;

    public ScnBIServiceImpl(JiraHome jiraHome) {
        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream(new File(jiraHome.getHome(), "scn-bi.properties"))) {
            props.load(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InternalRuntimeException("Cannot load scn-bi.properties");
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
            throw new InternalRuntimeException("Cannot connect to the SCN BI database");
        }
    }

    @Override
    public Map<Date, DayType> getUserCalendar(String username, @Nonnull LocalDate from, @Nonnull LocalDate to) {
        Map<Date, DayType> result = new HashMap<>();
        try {
            Class.forName(driverName);
            Connection con = DriverManager.getConnection(connection, login, password);
            try (PreparedStatement pstmt = con.prepareStatement("{call dbo.GetCalendar(?,?,?)}");) {
                pstmt.setString(1, username);
                pstmt.setString(2, from.toString());
                pstmt.setString(3, to.toString());
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
            } catch (Exception e) {
                throw new InternalRuntimeException(e);
            }
        } catch (Exception e) {
            throw new InternalRuntimeException(e);
        }

        return result;
    }
}
