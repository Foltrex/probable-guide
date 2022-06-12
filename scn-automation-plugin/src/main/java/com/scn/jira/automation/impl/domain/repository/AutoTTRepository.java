package com.scn.jira.automation.impl.domain.repository;

import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.common.ao.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.java.ao.Query.select;

@Repository
public class AutoTTRepository extends AbstractRepository<AutoTT, Long> {

    public List<AutoTT> findAllByActiveTrue() {
        return findAll(select().where("ACTIVE = ?", true));
    }

    public Optional<AutoTT> findByUserKey(String key) {
        return findAll(select().where("USER_KEY = ?", key)).stream().findFirst();
    }
}
