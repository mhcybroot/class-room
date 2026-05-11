package com.classroom.app.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_participants")
public class SessionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private VirtualRoom room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Column(nullable = false)
    private String displayName;

    private String phone;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();

    public Long getId() { return id; }
    public VirtualRoom getRoom() { return room; }
    public void setRoom(VirtualRoom room) { this.room = room; }
    public ParticipantRole getRole() { return role; }
    public void setRole(ParticipantRole role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
