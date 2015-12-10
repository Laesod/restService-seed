package com.repository.specification;

import com.entity.TaskEntity;
import com.repository.filter.TaskFilter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aautushk on 12/9/2015.
 */
public class TaskSpecification implements Specification<TaskEntity> {
    private final TaskFilter taskFilter;

    public TaskSpecification(TaskFilter taskFilter) {
        this.taskFilter = taskFilter;
    }

    @Override
    public Predicate toPredicate(Root<TaskEntity> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if(taskFilter.getProjects() != null) {
            predicates.add((root.get("project")).in(taskFilter.getProjects()));
        }

        if(taskFilter.getTypes() != null) {
            predicates.add((root.get("type")).in(taskFilter.getTypes()));
        }

        if(taskFilter.getStatuses() != null) {
            predicates.add((root.get("status")).in(taskFilter.getStatuses()));
        }

        if(taskFilter.getDescription() != null) {
            predicates.add(cb.like(cb.upper(root.get("description")), "%" + taskFilter.getDescription().toUpperCase() + "%"));
        }

        if(taskFilter.getCreatedByUser() != null) {
            predicates.add(cb.equal(root.get("createdByUser"), taskFilter.getCreatedByUser()));
        }

        if(predicates.size() > 0){
            return cb.and(predicates.toArray(new Predicate[0]));
        }
        else{
            return null;
        }
    }
}