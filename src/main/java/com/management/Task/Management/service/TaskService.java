package com.management.task.management.service;

import com.management.task.management.dto.TaskRequestDTO;
import com.management.task.management.dto.TaskResponseDTO;
import com.management.task.management.model.Task;
import com.management.task.management.model.User;
import com.management.task.management.repository.TaskRepository;
import com.management.task.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Get all tasks for current user
    public List<TaskResponseDTO> getAllTasks() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Create new task
    public Task createTask(TaskRequestDTO dto) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        task.setUser(user);

        return taskRepository.save(task);
    }

    // Update task
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        String currentUserEmail = getCurrentUserEmail();

        //Direct check
        if (!taskRepository.existsByIdAndUserEmail(id, currentUserEmail)) {
            throw new RuntimeException("Not authorized");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return mapToResponse(taskRepository.save(task));
    }

    // Delete task
    public void deleteTask(Long id) {
        String currentUserEmail = getCurrentUserEmail();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));


        if (!taskRepository.existsByIdAndUserEmail(id, currentUserEmail)) {
            throw new RuntimeException("Not authorized");
        }

        taskRepository.delete(task);
    }

    // Helper methods
    private String getCurrentUserEmail() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    private TaskResponseDTO mapToResponse(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate()
        );
    }
}