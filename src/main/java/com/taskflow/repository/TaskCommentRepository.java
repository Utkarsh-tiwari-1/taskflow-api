package com.taskflow.repository;

import com.taskflow.domain.TaskComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findAllByTask_IdOrderByCreatedAtAsc(Long taskId);
}
