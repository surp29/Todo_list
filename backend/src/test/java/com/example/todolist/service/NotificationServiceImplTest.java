package com.example.todolist.service;

import com.example.todolist.dto.NotificationResponseDTO;
import com.example.todolist.exception.NotificationNotFoundException;
import com.example.todolist.model.Notification;
import com.example.todolist.model.NotificationType;
import com.example.todolist.model.Role;
import com.example.todolist.model.Todo;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.repository.NotificationRepository;
import com.example.todolist.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User leader;
    private User employee;
    private Todo todo;

    @BeforeEach
    void setUp() {
        leader = User.builder()
                .id(1L)
                .username("truongnhom")
                .fullName("Tran Van Truong")
                .role(Role.LEADER)
                .active(true)
                .build();

        employee = User.builder()
                .id(2L)
                .username("nhanvien")
                .fullName("Nguyen Van A")
                .role(Role.EMPLOYEE)
                .active(true)
                .build();

        todo = Todo.builder()
                .id(10L)
                .title("Viet bao cao")
                .status(TodoStatus.COMPLETED)
                .priority(TodoPriority.HIGH)
                .assignee(employee)
                .createdBy(leader)
                .build();
    }

    @Test
    void test_notifyTaskCompleted_savesAndPushesToLeader() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });

        notificationService.notifyTaskCompleted(todo);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getRecipient()).isEqualTo(leader);
        assertThat(saved.getActor()).isEqualTo(employee);
        assertThat(saved.getRelatedTodoId()).isEqualTo(10L);
        assertThat(saved.getTodoTitle()).isEqualTo("Viet bao cao");
        assertThat(saved.getMessage()).contains("Nguyen Van A").contains("Viet bao cao");
        assertThat(saved.getType()).isEqualTo(NotificationType.TASK_COMPLETED);
        assertThat(saved.isRead()).isFalse();

        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("truongnhom"), eq("/queue/notifications"), any(NotificationResponseDTO.class));
    }

    @Test
    void test_notifyTaskAssigned_savesAndPushesToEmployee() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(101L);
            return n;
        });

        notificationService.notifyTaskAssigned(todo);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getRecipient()).isEqualTo(employee);
        assertThat(saved.getActor()).isEqualTo(leader);
        assertThat(saved.getRelatedTodoId()).isEqualTo(10L);
        assertThat(saved.getMessage()).contains("Tran Van Truong").contains("Viet bao cao");
        assertThat(saved.getType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(saved.isRead()).isFalse();

        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("nhanvien"), eq("/queue/notifications"), any(NotificationResponseDTO.class));
    }

    @Test
    void test_listForUser_returnsMappedNotificationsForRecipient() {
        Notification notification = Notification.builder()
                .id(100L)
                .recipient(leader)
                .actor(employee)
                .relatedTodoId(10L)
                .todoTitle("Viet bao cao")
                .message("Nguyen Van A da hoan thanh cong viec \"Viet bao cao\"")
                .isRead(false)
                .build();
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(leader)).thenReturn(List.of(notification));

        List<NotificationResponseDTO> result = notificationService.listForUser(leader);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getActorName()).isEqualTo("Nguyen Van A");
        assertThat(result.get(0).isRead()).isFalse();
        // Pre-existing notifications with no `type` column value default to TASK_COMPLETED.
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.TASK_COMPLETED);
    }

    @Test
    void test_markAsRead_success() {
        Notification notification = Notification.builder()
                .id(100L)
                .recipient(leader)
                .isRead(false)
                .build();
        when(notificationRepository.findByIdAndRecipient(100L, leader)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(100L, leader);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void test_markAsRead_notFound_throwsException() {
        when(notificationRepository.findByIdAndRecipient(999L, leader)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, leader))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void test_markAllAsRead_delegatesToRepositoryBulkUpdate() {
        notificationService.markAllAsRead(leader);

        verify(notificationRepository, times(1)).markAllAsRead(leader);
    }
}
