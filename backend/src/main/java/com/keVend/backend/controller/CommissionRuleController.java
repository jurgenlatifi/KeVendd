package com.keVend.backend.controller;

import com.keVend.backend.model.CommissionRule;
import com.keVend.backend.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only — registered under /api/v1/admin/** so SecurityConfig restricts access. */
@RestController
@RequestMapping("/api/v1/admin/commission-rules")
@RequiredArgsConstructor
public class CommissionRuleController {

    private final CommissionRuleRepository ruleRepository;

    @GetMapping
    public ResponseEntity<List<CommissionRule>> list() {
        return ResponseEntity.ok(ruleRepository.findAllByOrderByPriorityDescIdAsc());
    }

    @PostMapping
    public ResponseEntity<CommissionRule> create(@RequestBody CommissionRule rule) {
        rule.setId(null);
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommissionRule> update(@PathVariable Long id, @RequestBody CommissionRule rule) {
        rule.setId(id);
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
