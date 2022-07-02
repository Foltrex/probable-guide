package com.scn.jira.automation.impl.domain.service;

import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.common.exception.InternalRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScnBIServiceImpl implements ScnBIService {
    private static final String DRIVER_NAME = "net.sourceforge.jtds.jdbc.Driver";
    private static final String CONNECTION = "jdbc:jtds:sqlserver://SRV-BI:1433;DatabaseName=Jira_DWH;domain=MAIN";
    private static final String LOGIN = "sps-training-admin";
    private static final String PASSWORD = "06#$XPvf";

    @Override
    public Map<Date, DayType> getUserCalendar(String username, @Nonnull LocalDate from, @Nonnull LocalDate to) {
        Map<Date, DayType> result = new HashMap<>();
        try {
            Class.forName(DRIVER_NAME);
            Connection con = DriverManager.getConnection(CONNECTION, LOGIN, PASSWORD);
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
