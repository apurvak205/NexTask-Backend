package com.management.Task.Management.service;

import com.management.Task.Management.dto.TaskRequestDTO;
import com.management.Task.Management.dto.TaskResponseDTO;
import com.management.Task.Management.model.Task;
import com.management.Task.Management.model.User;
import com.management.Task.Management.repository.TaskRepository;
import com.management.Task.Management.repository.UserRepository;
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

    // Update existing task
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Security check: only owner can update
        String currentUserEmail = getCurrentUserEmail();
        if (!task.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Not authorized to update this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return mapToResponse(taskRepository.save(task));
    }

    // Delete task
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Security check: only owner can delete
        String currentUserEmail = getCurrentUserEmail();
        if (!task.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Not authorized to delete this task");
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