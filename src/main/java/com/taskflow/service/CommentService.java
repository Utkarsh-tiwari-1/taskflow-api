package com.taskflow.service;

import com.taskflow.domain.AppUser;
import com.taskflow.domain.Role;
import com.taskflow.domain.Task;
import com.taskflow.domain.TaskComment;
import com.taskflow.dto.CommentDtos.CommentResponse;
import com.taskflow.repository.TaskCommentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final TaskService taskService;
    private final TaskCommentRepository comments;

    public CommentService(TaskService taskService, TaskCommentRepository comments) {
        this.taskService = taskService;
        this.comments = comments;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long actorId, Role role, Long taskId) {
        taskService.findAccessibleEntity(actorId, role, taskId);
        return comments.findAllByTask_IdOrderByCreatedAtAsc(taskId).stream().map(CommentService::toResponse).toList();
    }

    @Transactional
    public CommentResponse add(Long actorId, Role role, Long taskId, String body) {
        Task task = taskService.findAccessibleEntity(actorId, role, taskId);
        AppUser author = taskService.findActor(actorId);
        return toResponse(comments.save(new TaskComment(task, author, body.trim())));
    }

    private static CommentResponse toResponse(TaskComment comment) {
        return new CommentResponse(comment.getId(), comment.getTask().getId(), comment.getAuthor().getId(),
                comment.getBody(), comment.getCreatedAt());
    }
}
