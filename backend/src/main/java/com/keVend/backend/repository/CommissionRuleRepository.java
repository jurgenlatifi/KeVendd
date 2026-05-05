package com.keVend.backend.repository;

import com.keVend.backend.model.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    /** Loaded once per request and filtered in-memory; the table is small. */
    List<CommissionRule> findAllByOrderByPriorityDescIdAsc();
}
