package com.scn.jira.automation.impl.domain.repository;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.common.ao.AbstractRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static net.java.ao.Query.select;

@PublicApi
@Repository
public class AutoTTRepository extends AbstractRepository<AutoTT, Long> {

    public List<AutoTT> findAllByActiveTrue() {
        return findAll(select().where("ACTIVE = ?", true));
    }

    public Optional<AutoTT> findByUserKey(String key) {
        return findAll(select().where("USER_KEY = ?", key)).stream().findFirst();
    }

    public void deleteAllByIssueId(Long issueId) {
        deleteAll(findAll(select().where("ISSUE_ID = ?", issueId)));
    }

    public void deleteAllByProjectId(Long projectId) {
        deleteAll(findAll(select().where("PROJECT_ID = ?", projectId)));
    }

    public void deleteAllByUsernameIn(@Nonnull Collection<String> username) {
        deleteAll(findAll(select().where("USERNAME IN (" + String.join(",", username) + ")")));
    }
}
