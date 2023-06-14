package com.scn.jira.worklog.impl.domain.repository;

import com.scn.jira.common.ao.AbstractRepository;
import com.scn.jira.worklog.impl.domain.entity.WLType;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.java.ao.Query.select;

@Repository
public class WLTypeRepository extends AbstractRepository<WLType, Long> {
    public List<WLType> findAllSortedByColumnInOrder(String column, String order) {
        return findAll(select().order(column + " " + order));
    }

}
