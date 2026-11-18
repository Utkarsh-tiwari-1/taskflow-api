package com.taskflow.service;

import com.taskflow.domain.AppUser;
import com.taskflow.domain.Role;
import com.taskflow.domain.Task;
import com.taskflow.domain.TaskPriority;
import com.taskflow.domain.TaskStatus;
import com.taskflow.dto.TaskDtos.CreateTaskRequest;
import com.taskflow.dto.TaskDtos.PageResponse;
import com.taskflow.dto.TaskDtos.TaskResponse;
import com.taskflow.dto.TaskDtos.UpdateTaskRequest;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.web.ApiExceptions.Forbidden;
import com.taskflow.web.ApiExceptions.NotFound;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional
    public TaskResponse create(Long actorId, CreateTaskRequest request) {
        AppUser actor = findUser(actorId);
        Task task = new Task(request.title().trim(), cleanDescription(request.description()),
                request.priority(), request.dueAt(), actor);
        if (request.assigneeId() != null) task.assign(findUser(request.assigneeId()));
        return toResponse(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(Long actorId, Role role, TaskStatus status,
                                           TaskPriority priority, Long assigneeId,
                                           String search, Pageable pageable) {
        List<Specification<Task>> filters = new ArrayList<>();
        if (role != Role.ADMIN) {
            filters.add((root, query, cb) -> cb.or(
                    cb.equal(root.get("createdBy").get("id"), actorId),
                    cb.equal(root.get("assignee").get("id"), actorId)));
        }
        if (status != null) filters.add((root, query, cb) -> cb.equal(root.get("status"), status));
        if (priority != null) filters.add((root, query, cb) -> cb.equal(root.get("priority"), priority));
        if (assigneeId != null) filters.add((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        if (search != null && !search.isBlank()) {
            String needle = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            filters.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), needle),
                    cb.like(cb.lower(root.get("description")), needle)));
        }
        Specification<Task> specification = Specification.allOf(filters);
        Page<Task> page = tasks.findAll(specification, pageable);
        return new PageResponse<>(page.map(TaskService::toResponse).getContent(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long actorId, Role role, Long taskId) {
        return toResponse(findAccessible(actorId, role, taskId));
    }

    @Transactional
    public TaskResponse update(Long actorId, Role role, Long taskId, UpdateTaskRequest request) {
        Task task = findAccessible(actorId, role, taskId);
        requireEditor(task, actorId, role);
        task.update(request.title().trim(), cleanDescription(request.description()), request.priority(), request.dueAt());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse changeStatus(Long actorId, Role role, Long taskId, TaskStatus status) {
        Task task = findAccessible(actorId, role, taskId);
        requireEditor(task, actorId, role);
        task.changeStatus(status);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse assign(Long actorId, Role role, Long taskId, Long assigneeId) {
        Task task = findAccessible(actorId, role, taskId);
        requireCreator(task, actorId, role);
        task.assign(assigneeId == null ? null : findUser(assigneeId));
        return toResponse(task);
    }

    @Transactional
    public void delete(Long actorId, Role role, Long taskId) {
        Task task = findAccessible(actorId, role, taskId);
        requireCreator(task, actorId, role);
        tasks.delete(task);
    }

    @Transactional(readOnly = true)
    public Task findAccessibleEntity(Long actorId, Role role, Long taskId) {
        return findAccessible(actorId, role, taskId);
    }

    @Transactional(readOnly = true)
    public AppUser findActor(Long actorId) { return findUser(actorId); }

    private Task findAccessible(Long actorId, Role role, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(() -> new NotFound("Task was not found"));
        if (role != Role.ADMIN && !task.getCreatedBy().getId().equals(actorId)
                && (task.getAssignee() == null || !task.getAssignee().getId().equals(actorId))) {
            throw new NotFound("Task was not found");
        }
        return task;
    }

    private AppUser findUser(Long userId) {
        return users.findById(userId).orElseThrow(() -> new NotFound("User was not found"));
    }

    private static void requireEditor(Task task, Long actorId, Role role) {
        if (role != Role.ADMIN && !task.getCreatedBy().getId().equals(actorId)
                && (task.getAssignee() == null || !task.getAssignee().getId().equals(actorId))) {
            throw new Forbidden("Only the task creator, assignee, or an admin may edit this task");
        }
    }

    private static void requireCreator(Task task, Long actorId, Role role) {
        if (role != Role.ADMIN && !task.getCreatedBy().getId().equals(actorId)) {
            throw new Forbidden("Only the task creator or an admin may change its assignment or delete it");
        }
    }

    private static String cleanDescription(String description) {
        if (description == null || description.isBlank()) return null;
        return description.trim();
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getPriority(), task.getDueAt(), task.getCreatedBy().getId(),
                task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
