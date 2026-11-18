package com.taskflow.web;

import com.taskflow.dto.CommentDtos.CommentResponse;
import com.taskflow.dto.CommentDtos.CreateCommentRequest;
import com.taskflow.service.CommentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) { this.commentService = commentService; }

    @GetMapping
    public List<CommentResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
        Actor actor = Actor.from(jwt);
        return commentService.list(actor.id(), actor.role(), taskId);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> add(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId,
                                               @Valid @RequestBody CreateCommentRequest request) {
        Actor actor = Actor.from(jwt);
        CommentResponse comment = commentService.add(actor.id(), actor.role(), taskId, request.body());
        return ResponseEntity.created(URI.create("/api/tasks/" + taskId + "/comments/" + comment.id())).body(comment);
    }
}
