package com.scn.jira.worklog.listener;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.scn.jira.worklog.api.WorklogPluginComponent;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.utils.PluginManagerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j
@PublicApi
public class WorklogPluginLauncher implements LifecycleAware {
    private static Timestamp START_DATE;

    static {
        try {
            START_DATE = new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private final ProjectManager projectManager;
    private final IScnProjectSettingsManager projectSettingManager;
    private final WorklogPluginComponent worklogPluginComponent;
    private final OfBizConnectionFactory ofBizConnectionFactory = DefaultOfBizConnectionFactory.getInstance();
    private final OfBizDelegator delegator;
    private final PluginManagerUtils pluginManagerUtils;

    @Override
    public void onStart() {
        pluginManagerUtils.disableSystemPlugins();
        log.warn(worklogPluginComponent.getName() + " has been started.");
    }

    @Override
    public void onStop() {
        pluginManagerUtils.enableSystemPlugins();
    }

    private void migrateIncorrectScnWorklogs() {
        String QUERY = "select w.id, w.author, u.user_key\n" +
            "from worklog_scn w\n" +
            "         inner join (select * from app_user u where u.user_key != u.lower_user_name) u\n" +
            "                    on lower(w.author) = lower(u.user_key)\n" +
            "where w.startdate >= ?";
        try (Connection connection = ofBizConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {
            preparedStatement.setTimestamp(1, START_DATE);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String author = resultSet.getString("author");
                    String user_key = resultSet.getString("user_key");
                    if (!StringUtils.equals(author, user_key)) {
                        GenericValue scnWorklog = delegator.findByPrimaryKey("ScnWorklog", id);
                        scnWorklog.set("author", user_key);
                        scnWorklog.set("updateauthor", user_key);
                        scnWorklog.store();
                    }
                }
            }
        } catch (SQLException | GenericEntityException e) {
            throw new RuntimeException("SQL error.");
        }
    }

    private void migrateIncorrectWorklogs() {
        String QUERY = "select w.id, w.author, u.user_key\n" +
            "from worklog w\n" +
            "         inner join (select * from app_user u where u.user_key != u.lower_user_name) u\n" +
            "                    on lower(w.author) = lower(u.user_key)\n" +
            "where w.startdate >= ?";
        try (Connection connection = ofBizConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {
            preparedStatement.setTimestamp(1, START_DATE);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String author = resultSet.getString("author");
                    String user_key = resultSet.getString("user_key");
                    if (!StringUtils.equals(author, user_key)) {
                        GenericValue scnWorklog = delegator.findByPrimaryKey("Worklog", id);
                        scnWorklog.set("author", user_key);
                        scnWorklog.set("updateauthor", user_key);
                        scnWorklog.store();
                    }
                }
            }
        } catch (SQLException | GenericEntityException e) {
            throw new RuntimeException("SQL error.");
        }
    }

    private void updateProjectsSettings() {
        List<Project> projects = projectManager.getProjectObjects();
        projects.forEach(project -> projectSettingManager.setWLTypeRequired(project.getId(), true));
    }
}
