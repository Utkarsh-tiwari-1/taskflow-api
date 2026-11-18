package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CommentDtos {
    private CommentDtos() { }

    public record CreateCommentRequest(@NotBlank @Size(max = 2000) String body) { }
    public record CommentResponse(Long id, Long taskId, Long authorId, String body, Instant createdAt) { }
}
