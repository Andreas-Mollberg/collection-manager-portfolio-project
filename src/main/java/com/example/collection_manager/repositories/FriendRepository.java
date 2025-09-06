package com.example.collection_manager.repositories;

import com.example.collection_manager.enums.FriendStatus;
import com.example.collection_manager.models.Friend;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByRequesterIdAndRecipientId(Long requesterId, Long recipientId);

    boolean existsByRequesterIdAndRecipientIdAndStatus(Long requesterId, Long recipientId, FriendStatus status);

    @EntityGraph(attributePaths = {"requester", "recipient"})
    List<Friend> findAllByRequesterIdAndStatus(Long requesterId, FriendStatus status);

    @EntityGraph(attributePaths = {"requester", "recipient"})
    List<Friend> findAllByRecipientIdAndStatus(Long recipientId, FriendStatus status);
}
