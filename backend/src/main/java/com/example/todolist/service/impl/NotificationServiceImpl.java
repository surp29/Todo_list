package com.example.todolist.service.impl;

import com.example.todolist.dto.NotificationResponseDTO;
import com.example.todolist.exception.NotificationNotFoundException;
import com.example.todolist.model.Notification;
import com.example.todolist.model.Todo;
import com.example.todolist.model.User;
import com.example.todolist.repository.NotificationRepository;
import com.example.todolist.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link NotificationService}. Persists notifications
 * and pushes them in real time to the recipient's WebSocket queue.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final String QUEUE_DESTINATION = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTaskCompleted(Todo todo) {
        User leader = todo.getCreatedBy();
        User employee = todo.getAssignee();

        String message = String.format("%s đã hoàn thành công việc \"%s\"",
                employee.getFullName(), todo.getTitle());

        Notification notification = Notification.builder()
                .recipient(leader)
                .actor(employee)
                .relatedTodoId(todo.getId())
                .todoTitle(todo.getTitle())
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                leader.getUsername(), QUEUE_DESTINATION, NotificationResponseDTO.from(saved));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listForUser(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient).stream()
                .map(NotificationResponseDTO::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsRead(Long notificationId, User recipient) {
        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, recipient)
                .orElseThrow(() -> new NotificationNotFoundException("Không tìm thấy thông báo với id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
