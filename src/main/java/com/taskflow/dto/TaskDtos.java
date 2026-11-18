package com.taskflow.dto;

import com.taskflow.domain.TaskPriority;
import com.taskflow.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class TaskDtos {
    private TaskDtos() { }

    public record CreateTaskRequest(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 5000) String description,
            @NotNull TaskPriority priority,
            Instant dueAt,
            Long assigneeId) { }

    public record UpdateTaskRequest(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 5000) String description,
            @NotNull TaskPriority priority,
            Instant dueAt) { }

    public record ChangeStatusRequest(@NotNull TaskStatus status) { }
    public record AssignTaskRequest(Long assigneeId) { }

    public record TaskResponse(Long id, String title, String description, TaskStatus status,
                               TaskPriority priority, Instant dueAt, Long createdById,
                               Long assigneeId, Instant createdAt, Instant updatedAt) { }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements,
                                  int totalPages, boolean first, boolean last) { }
}
