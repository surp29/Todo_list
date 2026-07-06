package com.example.todolist.repository;

import com.example.todolist.model.Notification;
import com.example.todolist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Notification} entities.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    Optional<Notification> findByIdAndRecipient(Long id, User recipient);

    boolean existsByActor(User actor);

    boolean existsByRecipient(User recipient);

    /**
     * Detaches a user from any notification where they are the actor (e.g. "X completed
     * task Y"), used before permanently deleting their account so the FK constraint
     * doesn't block the delete. The notification message text already names them, so the
     * Leader's notification history stays readable even after the account is gone.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.actor = null WHERE n.actor = :actor")
    void clearActor(@Param("actor") User actor);

    /**
     * Deletes notifications addressed to this recipient (e.g. "you were assigned task X"),
     * used before permanently deleting their account. Unlike actor references, recipient is
     * a required field, so it can't just be nulled out — and a notification addressed to an
     * account that no longer exists has no reader left to show it to anyway.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient = :recipient")
    void deleteByRecipient(@Param("recipient") User recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsRead(@Param("recipient") User recipient);
}
