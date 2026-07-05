package com.example.todolist.repository;

import com.example.todolist.model.Notification;
import com.example.todolist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Notification} entities.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    Optional<Notification> findByIdAndRecipient(Long id, User recipient);
}
