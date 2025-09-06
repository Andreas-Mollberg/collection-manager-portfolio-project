package com.example.collection_manager.models;

import com.example.collection_manager.enums.FriendStatus;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "friends", uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "recipient_id"}))
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status = FriendStatus.PENDING;

    public Friend() {
    }

    public Friend(User requester, User recipient, FriendStatus status) {
        this.requester = requester;
        this.recipient = recipient;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public FriendStatus getStatus() {
        return status;
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return Objects.equals(id, friend.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}