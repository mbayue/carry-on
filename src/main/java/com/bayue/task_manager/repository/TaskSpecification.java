package com.bayue.task_manager.repository;

import com.bayue.task_manager.entity.Task;
import com.bayue.task_manager.entity.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> filter(TaskStatus status, LocalDate dueDate, UUID userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (dueDate != null) {
                LocalDateTime startOfDay = dueDate.atStartOfDay();
                LocalDateTime endOfDay = dueDate.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), startOfDay));
                predicates.add(cb.lessThan(root.get("dueDate"), endOfDay));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
