package com.taskflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "task_comments")
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TaskComment() { }

    public TaskComment(Task task, AppUser author, String body) {
        this.task = task;
        this.author = author;
        this.body = body;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public AppUser getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
