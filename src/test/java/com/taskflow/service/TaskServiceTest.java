package com.taskflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.taskflow.domain.AppUser;
import com.taskflow.domain.Role;
import com.taskflow.domain.Task;
import com.taskflow.domain.TaskPriority;
import com.taskflow.domain.TaskStatus;
import com.taskflow.dto.TaskDtos.CreateTaskRequest;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.web.ApiExceptions.NotFound;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository tasks;
    @Mock UserRepository users;
    @InjectMocks TaskService taskService;

    @Test
    void createTrimsFieldsAndStartsTaskAsOpen() {
        AppUser owner = user(1L);
        when(users.findById(1L)).thenReturn(Optional.of(owner));
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", 10L);
            return task;
        });

        var result = taskService.create(1L,
                new CreateTaskRequest("  Review API  ", "  check the contract  ", TaskPriority.HIGH, null, null));

        assertEquals(10L, result.id());
        assertEquals("Review API", result.title());
        assertEquals("check the contract", result.description());
        assertEquals(TaskStatus.OPEN, result.status());
        assertEquals(1L, result.createdById());
    }

    @Test
    void userCannotReadAnotherUsersTask() {
        AppUser owner = user(1L);
        Task task = new Task("Private", null, TaskPriority.LOW, null, owner);
        ReflectionTestUtils.setField(task, "id", 7L);
        when(tasks.findById(7L)).thenReturn(Optional.of(task));

        assertThrows(NotFound.class, () -> taskService.get(2L, Role.USER, 7L));
    }

    private static AppUser user(Long id) {
        AppUser user = new AppUser("user" + id + "@example.com", "hash", "User " + id, Role.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
