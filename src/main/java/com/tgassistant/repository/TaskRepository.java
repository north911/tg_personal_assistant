package com.tgassistant.repository;

import java.util.List;

import com.tgassistant.domain.Task;
import com.tgassistant.domain.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByType(TaskType type);

    /**
     * Scoped by type as well as id so a stale button cannot delete a task of another type.
     *
     * @return how many rows were removed, 0 when the task is already gone
     */
    long deleteByIdAndType(Long id, TaskType type);

    /**
     * @return how many rows were removed
     */
    long deleteByType(TaskType type);
}
