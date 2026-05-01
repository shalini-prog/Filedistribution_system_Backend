package com.yespackage.dfs.controller;

import com.yespackage.dfs.entity.Task;
import com.yespackage.dfs.entity.User;
import com.yespackage.dfs.repository.TaskRepository;
import com.yespackage.dfs.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository,
                          UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {

            // 🔥 Get logged-in username from Spring Security
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            task.setUser(user);

            Task savedTask = taskRepository.save(task);

            return ResponseEntity.ok(savedTask);

        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body("Error creating task: " + e.getMessage());
        }
    }
}