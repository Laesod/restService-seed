package com.repository;

import com.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by aautushk on 11/30/2015.
 */
@Repository
public interface ITaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor {
    Page<TaskEntity> findAll(Pageable pageable);
    TaskEntity findByTaskGuid(String taskGuid);

    Page<TaskEntity> findByProject(String project, Pageable pageable);

}
