package com.yespackage.dfs.repository;

import com.yespackage.dfs.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId); // Fetch tasks for a specific user
}