package com.example.collection_manager.services;

import com.example.collection_manager.enums.FriendStatus;
import com.example.collection_manager.models.Friend;
import com.example.collection_manager.models.User;
import com.example.collection_manager.repositories.FriendRepository;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    private boolean friendshipExists(Long a, Long b) {
        return friendRepository.findByRequesterIdAndRecipientId(a, b).isPresent()
                || friendRepository.findByRequesterIdAndRecipientId(b, a).isPresent();
    }

    @Transactional
    public boolean sendRequest(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) return false;
        if (friendshipExists(requesterId, recipientId)) return false;

        Optional<User> req = userRepository.findById(requesterId);
        Optional<User> rec = userRepository.findById(recipientId);
        if (req.isEmpty() || rec.isEmpty()) return false;

        friendRepository.save(new Friend(req.get(), rec.get(), FriendStatus.PENDING));
        return true;
    }

    @Transactional
    public boolean acceptRequest(Long requesterId, Long recipientId) {
        Optional<Friend> opt = friendRepository.findByRequesterIdAndRecipientId(requesterId, recipientId);
        if (opt.isPresent() && opt.get().getStatus() == FriendStatus.PENDING) {
            Friend f = opt.get();
            f.setStatus(FriendStatus.ACCEPTED);
            friendRepository.save(f);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean declineOrCancel(Long requesterId, Long recipientId) {
        Optional<Friend> opt = friendRepository.findByRequesterIdAndRecipientId(requesterId, recipientId);
        opt.ifPresent(friendRepository::delete);
        return opt.isPresent();
    }

    @Transactional
    public boolean removeFriend(Long userA, Long userB) {
        Optional<Friend> found =
                friendRepository.findByRequesterIdAndRecipientId(userA, userB)
                        .or(() -> friendRepository.findByRequesterIdAndRecipientId(userB, userA));

        if (found.isPresent()) {
            friendRepository.delete(found.get());
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean areFriends(Long userA, Long userB) {
        return friendRepository.existsByRequesterIdAndRecipientIdAndStatus(userA, userB, FriendStatus.ACCEPTED)
                || friendRepository.existsByRequesterIdAndRecipientIdAndStatus(userB, userA, FriendStatus.ACCEPTED);
    }

    @Transactional(readOnly = true)
    public List<Friend> incomingRequests(Long userId) {
        return friendRepository.findAllByRecipientIdAndStatus(userId, FriendStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Friend> outgoingRequests(Long userId) {
        return friendRepository.findAllByRequesterIdAndStatus(userId, FriendStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Friend> friendsOf(Long userId) {
        List<Friend> reqSide = friendRepository.findAllByRequesterIdAndStatus(userId, FriendStatus.ACCEPTED);
        List<Friend> recSide = friendRepository.findAllByRecipientIdAndStatus(userId, FriendStatus.ACCEPTED);
        List<Friend> merged = new ArrayList<>(reqSide.size() + recSide.size());
        merged.addAll(reqSide);
        merged.addAll(recSide);
        return merged;
    }
}
