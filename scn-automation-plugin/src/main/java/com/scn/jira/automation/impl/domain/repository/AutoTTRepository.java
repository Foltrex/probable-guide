package com.scn.jira.automation.impl.domain.repository;

import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.common.ao.AbstractRepository;
import net.java.ao.DBParam;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.java.ao.Query.select;

@Repository
public class AutoTTRepository extends AbstractRepository<AutoTT, Long> {
    public AutoTT create(String userKey, Long projectId, Long issueId, Long ratedTime) {
        return create(new DBParam("USER_KEY", userKey), new DBParam("PROJECT_ID", projectId), new DBParam("ISSUE_ID", issueId), new DBParam("RATED_TIME", ratedTime));
    }

    public List<AutoTT> findAllByActiveTrue() {
        return findAll(select().where("ACTIVE = ?", true));
    }

    public Optional<AutoTT> findByByUserKey(String key) {
        return findAll(select().where("USER_KEY = ?", key)).stream().findFirst();
    }
}
