package com.taskflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    protected Task() { }

    public Task(String title, String description, TaskPriority priority, Instant dueAt, AppUser createdBy) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.OPEN;
        this.priority = priority;
        this.dueAt = dueAt;
        this.createdBy = createdBy;
    }

    public void update(String title, String description, TaskPriority priority, Instant dueAt) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueAt = dueAt;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(TaskStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public void assign(AppUser assignee) { this.assignee = assignee; this.updatedAt = Instant.now(); }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; }
    public Instant getDueAt() { return dueAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public AppUser getCreatedBy() { return createdBy; }
    public AppUser getAssignee() { return assignee; }
}
