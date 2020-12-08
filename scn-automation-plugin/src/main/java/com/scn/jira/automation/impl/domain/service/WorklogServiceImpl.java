package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.scn.jira.automation.api.domain.service.WorklogService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogKind;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorklogServiceImpl implements WorklogService {
    private final OfBizConnectionFactory ofBizConnectionFactory = DefaultOfBizConnectionFactory.getInstance();

    @Override
    public List<WorklogDto> getAllByProject(Long projectId) {
        List<WorklogDto> result = new ArrayList<>();
        String querySQL = "select " +
            "worklog.*, " +
            "jiraissue.project, " +
            "worklog_worklogtype_scn.worklogtype " +
            "from worklog " +
            "left join jiraissue " +
            "on worklog.issueid = jiraissue.id " +
            "left join worklog_worklogtype_scn " +
            "on worklog.id = worklog_worklogtype_scn.id " +
            "where jiraissue.project = ?";
        try (Connection connection = ofBizConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setLong(1, projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    WorklogDto worklog = new WorklogDto();
                    worklog.setWorklogKind(WorklogKind.WORKLOG);
                    this.copySQLFields(worklog, resultSet);
                    result.add(worklog);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get worklogs by SQL query.");
        }

        return result;
    }

    @Override
    public List<WorklogDto> getAllScnByProject(Long projectId) {
        List<WorklogDto> result = new ArrayList<>();
        String querySQL = "select " +
            "worklog_scn.*, " +
            "jiraissue.project " +
            "from worklog_scn " +
            "left join jiraissue " +
            "on worklog_scn.issueid = jiraissue.id " +
            "where jiraissue.project = ?";
        try (Connection connection = ofBizConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setLong(1, projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    WorklogDto worklog = new WorklogDto();
                    worklog.setWorklogKind(WorklogKind.WORKLOG_SCN);
                    this.copySQLFields(worklog, resultSet);
                    worklog.setLinkedWorklogId(resultSet.getLong("WORKLOG_ID"));
                    result.add(worklog);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get scnWorklogs by SQL query.");
        }

        return result;
    }

    private void copySQLFields(@Nonnull WorklogDto worklog, @Nonnull ResultSet resultSet) throws SQLException {
        worklog.setId(resultSet.getLong("ID"));
        worklog.setIssueId(resultSet.getLong("ISSUEID"));
        worklog.setProjectId(resultSet.getLong("PROJECT"));
        worklog.setWorklogBody(resultSet.getString("WORKLOGBODY"));
        worklog.setStartDate(resultSet.getTimestamp("STARTDATE"));
        worklog.setTimeWorked(resultSet.getLong("TIMEWORKED"));
        worklog.setWorklogTypeId(resultSet.getString("WORKLOGTYPE"));
        worklog.setAuthorKey(resultSet.getString("AUTHOR"));
        worklog.setUpdateAuthorKey(resultSet.getString("UPDATEAUTHOR"));
        worklog.setCreated(resultSet.getTimestamp("CREATED"));
        worklog.setUpdated(resultSet.getTimestamp("UPDATED"));
    }
}
