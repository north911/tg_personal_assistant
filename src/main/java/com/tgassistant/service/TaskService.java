package com.tgassistant.service;

import java.util.List;

import com.tgassistant.domain.Task;
import com.tgassistant.domain.TaskType;
import com.tgassistant.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Stores one {@link Task} per description, all sharing the given type.
     */
    @Transactional
    public List<Task> addTasks(List<String> descriptions, TaskType type) {
        List<Task> tasks = descriptions.stream()
                .map(description -> new Task(description, type))
                .toList();
        return taskRepository.saveAll(tasks);
    }
}