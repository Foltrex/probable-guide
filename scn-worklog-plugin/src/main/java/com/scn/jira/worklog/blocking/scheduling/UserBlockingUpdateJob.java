package com.scn.jira.worklog.blocking.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;

public class UserBlockingUpdateJob implements PluginJob
{
	private static final Logger log = LoggerFactory.getLogger(UserBlockingUpdateJob.class);
	
	private final IScnUserBlockingManager userBlockingManager;
	
	public UserBlockingUpdateJob(
			IScnUserBlockingManager userBlockingManager)
	{
		this.userBlockingManager = userBlockingManager;
	}
	
	public void execute(Map<String, Object> jobDataMap)
	{
		HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(userBlockingManager.getSettingsUrl());
		
		try
		{
			int statusCode = httpClient.executeMethod(get);
			
			if (statusCode != HttpStatus.SC_OK) 
			{
				log.error("Scheduler was not able recieve user blockings from url.");
				return;
			}
			
			InputStream is = get.getResponseBodyAsStream();
			
			userBlockingManager.setAll(parse(is));
		}
		catch (Exception e)
		{
			log.error("Error ocurred during user blocking update. User blocking settings could be not updated.", e);
		}
		finally 
		{
			get.releaseConnection();
		}
	}
	
	private Map<String, Date> parse(InputStream is) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		UserListHandler handler = new UserListHandler();
		parser.parse(is, handler);
		
		return handler.getSettings();
	}
	
	private class UserListHandler extends DefaultHandler
	{
		private Map<String, Date> settings = new HashMap<String, Date>();
		
		private String elementName;
		private String name;
		private Date date;
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			this.elementName = qName;
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			String value = new String(ch, start, length).trim();
			if ("name".equalsIgnoreCase(elementName))
			{
				this.name = value;
			}
			else if ("date".equalsIgnoreCase(elementName))
			{
				this.date = userBlockingManager.parse(value);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if ("user".equalsIgnoreCase(qName))
			{
				settings.put(this.name, this.date);
			}
		}
		
		public Map<String, Date> getSettings()
		{
			return this.settings;
		}
	}
}
