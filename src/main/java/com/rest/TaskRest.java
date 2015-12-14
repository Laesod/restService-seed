package com.rest;

import com.CurrentUserInfo;
import com.dto.TaskRequestDto;
import com.dto.TaskResponseDto;
import com.entity.TaskEntity;
import com.repository.ITaskRepository;
import com.repository.filter.TaskFilter;
import com.repository.specification.TaskSpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aautushk on 11/30/2015.
 */

@Component
@RestController
@RequestMapping("/tasks-rest")
public class TaskRest {
    @Autowired
    public ITaskRepository taskRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private ModelMapper modelMapper = new ModelMapper(); //read more at http://modelmapper.org/user-manual/

    @RequestMapping(value = "/getTasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Page<TaskResponseDto> getTasks(@PageableDefault Pageable pageable,
           @RequestParam(value = "project", required = false) List<String> project,
           @RequestParam(value = "type", required = false) List<String> type,
           @RequestParam(value = "status", required = false) List<String> status,
           @RequestParam(value = "description", required = false) String description,
           @RequestParam(value = "createdByUser") String createdByUser) {

        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setProjects(project);
        taskFilter.setTypes(type);
        taskFilter.setStatuses(status);
        taskFilter.setDescription(description);
        taskFilter.setCreatedByUser(createdByUser);

        TaskSpecification taskSpecification = new TaskSpecification(taskFilter);
        final Page<TaskEntity> taskEntities = taskRepository.findAll(taskSpecification, pageable);

        List<TaskResponseDto> taskResponseDtos = new ArrayList<TaskResponseDto>();
        if (taskEntities != null) {
            for (TaskEntity taskEntity : taskEntities.getContent()) {
                TaskResponseDto taskResponseDto = new TaskResponseDto();

                taskResponseDto = modelMapper.map(taskEntity, TaskResponseDto.class);

                taskResponseDtos.add(taskResponseDto);
            }
        }

        Page<TaskResponseDto> page = new PageImpl<>(taskResponseDtos, pageable, taskEntities.getTotalElements());
        return page;
    }

    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        CurrentUserInfo.userName = taskRequestDto.getUserName();
        TaskEntity taskEntity = new TaskEntity();

        taskEntity = modelMapper.map(taskRequestDto, TaskEntity.class);
        taskRepository.save(taskEntity);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/changeTask/{taskGuid}", method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity changeTask(@PathVariable String taskGuid, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        CurrentUserInfo.userName = taskRequestDto.getUserName();
        TaskEntity taskEntity = taskRepository.findByTaskGuid(taskGuid);

        if(!CurrentUserInfo.userName.equals(taskEntity.getCreatedByUser())){
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        modelMapper.map(taskRequestDto, taskEntity);
        taskRepository.save(taskEntity);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteTask/{taskGuid}", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity deleteTask(@PathVariable String taskGuid, @RequestParam(value = "createdByUser") String createdByUser) {
        CurrentUserInfo.userName = createdByUser;

        TaskEntity taskEntity = taskRepository.findByTaskGuid(taskGuid);

        if(taskEntity == null){
            return new ResponseEntity(HttpStatus.NO_CONTENT);  // nothing to delete, check more info at http://stackoverflow.com/questions/2342579/http-status-code-for-update-and-delete
        }

        if(!CurrentUserInfo.userName.equals(taskEntity.getCreatedByUser())){
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        taskRepository.delete(taskEntity);

        return new ResponseEntity(HttpStatus.OK);
    }
}