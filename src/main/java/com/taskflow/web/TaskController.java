package com.taskflow.web;

import com.taskflow.domain.TaskPriority;
import com.taskflow.domain.TaskStatus;
import com.taskflow.dto.TaskDtos.AssignTaskRequest;
import com.taskflow.dto.TaskDtos.ChangeStatusRequest;
import com.taskflow.dto.TaskDtos.CreateTaskRequest;
import com.taskflow.dto.TaskDtos.PageResponse;
import com.taskflow.dto.TaskDtos.TaskResponse;
import com.taskflow.dto.TaskDtos.UpdateTaskRequest;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CreateTaskRequest request) {
        Actor actor = Actor.from(jwt);
        TaskResponse created = taskService.create(actor.id(), request);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<TaskResponse> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Actor actor = Actor.from(jwt);
        return taskService.list(actor.id(), actor.role(), status, priority, assigneeId, search, pageable);
    }

    @GetMapping("/{taskId}")
    public TaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
        Actor actor = Actor.from(jwt);
        return taskService.get(actor.id(), actor.role(), taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        Actor actor = Actor.from(jwt);
        return taskService.update(actor.id(), actor.role(), taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId,
                                     @Valid @RequestBody ChangeStatusRequest request) {
        Actor actor = Actor.from(jwt);
        return taskService.changeStatus(actor.id(), actor.role(), taskId, request.status());
    }

    @PatchMapping("/{taskId}/assignment")
    public TaskResponse assign(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId,
                               @RequestBody AssignTaskRequest request) {
        Actor actor = Actor.from(jwt);
        return taskService.assign(actor.id(), actor.role(), taskId, request.assigneeId());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
        Actor actor = Actor.from(jwt);
        taskService.delete(actor.id(), actor.role(), taskId);
        return ResponseEntity.noContent().build();
    }
}
