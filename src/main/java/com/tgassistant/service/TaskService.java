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

    /**
     * Every stored {@link Task} of the given type, oldest first.
     */
    @Transactional(readOnly = true)
    public List<Task> tasksOfType(TaskType type) {
        return taskRepository.findByType(type);
    }

    /**
     * Removes one task, provided it really is of the given type.
     *
     * @return whether it was there to remove — false when an old button names a task that
     *         has since been deleted
     */
    @Transactional
    public boolean deleteTask(long id, TaskType type) {
        return taskRepository.deleteByIdAndType(id, type) > 0;
    }

    /**
     * @return how many tasks were removed
     */
    @Transactional
    public long deleteAllOfType(TaskType type) {
        return taskRepository.deleteByType(type);
    }
}
