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

    /**
     * Detaches a user from any notification where they are the actor (e.g. "X completed
     * task Y"), used before permanently deleting their account so the FK constraint
     * doesn't block the delete. The notification message text already names them, so the
     * Leader's notification history stays readable even after the account is gone.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.actor = null WHERE n.actor = :actor")
    void clearActor(@Param("actor") User actor);
}
