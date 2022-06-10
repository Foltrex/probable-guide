package com.scn.jira.automation.impl.domain.repository;

import com.scn.jira.automation.impl.domain.entity.Worklog;
import com.scn.jira.common.ao.AbstractRepository;
import net.java.ao.DBParam;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.java.ao.Query.select;

public abstract class AbstractWorklogBackupRepository<W extends Worklog> extends AbstractRepository<W, Long> {

    public W create(Long id, Long issueId, Long projectId) {
        return create(new DBParam("ID", id), new DBParam("ISSUE_ID", issueId), new DBParam("PROJECT_ID", projectId));
    }

    public List<W> findAllByProjectIdAndStartDateBetween(@Nonnull Long projectId, Timestamp from, Timestamp to) {
        Object[] params = Stream.of(projectId, from != null ? from : to, to).filter(Objects::nonNull).toArray();
        return findAll(select().where("PROJECT_ID = ? AND START_DATE >= ? AND START_DATE <= ?"
                .replaceFirst("AND START_DATE >= \\?", from == null ? "" : "AND START_DATE >= ?")
                .replaceFirst("AND START_DATE <= \\?", to == null ? "" : "AND START_DATE <= ?")
            , params));
    }

    public void deleteAllByProjectIdAndStartDateBetween(@Nonnull Long projectId, Timestamp from, Timestamp to) {
        deleteAll(findAllByProjectIdAndStartDateBetween(projectId, from, to));
    }
}
