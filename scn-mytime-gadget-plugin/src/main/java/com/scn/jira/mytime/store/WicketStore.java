package com.scn.jira.mytime.store;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.scn.jira.mytime.util.DateUtils;

import javax.inject.Inject;
import javax.inject.Named;

public class WicketStore {
	
	private I18nHelper i18nResolver;
	private static String driverName;
	private static String connection;
	private static String loginTatsi;
	private static String password;

	public WicketStore(I18nHelper i18nResolver) {
		super();
		this.i18nResolver = i18nResolver;
	}
	
	private void getDbProperties() {
		driverName = i18nResolver.getText("mytime.gadget.driverName");
		connection = i18nResolver.getText("mytime.gadget.connection");
		loginTatsi = i18nResolver.getText("mytime.gadget.login");
		password = i18nResolver.getText("mytime.gadget.password");
	}
	
	public Map<String, Long> gerUserWicketTimeForthePeriod(String login, Date startDate, Date endDate) {
		getDbProperties();
		Map<String, Long> timesMap = new HashMap<String, Long>();
		
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
			
			/*
			 * String sql =
			 * "SELECT TOP 100 [ID], [Person],[Begin],[End],[Total_time],[Office_time],[Events_count],[Adjusted],[Created_at],[Updated_at],"
			 * +
			 * "[Day_begin],[Day_duration],[Ll],[Lc],[Day],[Approx],[Overnight],[SourceTypeID] FROM [Jira_DWH].[dbo].[WorkDays]"
			 * + " where Person ='"+ loginTatsi + "' and Day >='" + startDay
			 * +"' and Day<='"+endDay+"'";
			 */
			
			String sql = "SELECT TOP 100 [ID], [Person],[Office_time],[Day] FROM [Jira_DWH].[dbo].[WorkDays]" + " where Person ='" + login
					+ "' and Day >='" + startDay + "' and Day<='" + endDay + "'";
			
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				String person = rs.getString("Person");
				long office_time = rs.getLong("Office_time");
				String day = rs.getString("Day");
				
				timesMap.put(day, office_time);
				
				/*
				 * int supplierID = rs.getInt("ID"); long total =
				 * rs.getLong("Total_time"); String created =
				 * rs.getString("Created_at"); String begin =
				 * rs.getString("Begin"); String begiEndn = rs.getString("End");
				 * long events_count = rs.getLong("Events_count"); String
				 * adjusted = rs.getString("Adjusted"); String updated_at =
				 * rs.getString("Updated_at"); String day_begin =
				 * rs.getString("day_begin"); String day_duration =
				 * rs.getString("day_duration"); String lt = rs.getString("Ll");
				 * String lc = rs.getString("Lc");
				 * 
				 * String approx = rs.getString("Approx"); String overnight =
				 * rs.getString("Overnight"); String sourceTypeID =
				 * rs.getString("SourceTypeID");
				 */
				
				/*
				 * System.out.println("*****************************");
				 * System.out.println("supplierID: "+supplierID);
				 * System.out.println("Person: "+person);
				 * System.out.println("total: "+DateUtils.timeToString(total));
				 * System.out.println("created: "+begiEndn);
				 * System.out.println("begiEndn: "+begin);
				 * System.out.println("office_time: "
				 * +DateUtils.timeToString(office_time));
				 * System.out.println("events_count: "+events_count);
				 * System.out.println("adjusted: "+adjusted);
				 * System.out.println("events_count: "+events_count);
				 * System.out.println("adjusted: "+adjusted);
				 * System.out.println("updated_at: "+updated_at);
				 * System.out.println("day_begin: "+day_begin);
				 * System.out.println("day_duration: "+day_duration);
				 * System.out.println("lt: "+lt); System.out.println("lc: "+lc);
				 * System.out.println("day: "+day);
				 * System.out.println("approx: "+approx);
				 * System.out.println("overnight: "+overnight);
				 * System.out.println("sourceTypeID: "+sourceTypeID);
				 */
			}
			
			rs.close();
			s.close();
			con.close();
			
			return timesMap;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return timesMap;
	}
	
}