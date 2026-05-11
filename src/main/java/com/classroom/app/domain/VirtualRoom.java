package com.classroom.app.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "virtual_rooms")
public class VirtualRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Classroom classroom;

    @ManyToOne(optional = false)
    private Teacher teacher;

    @Column(nullable = false, unique = true)
    private String roomCode;

    @Column(nullable = false)
    private String roomPin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.SCHEDULED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public static String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public Long getId() { return id; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getRoomPin() { return roomPin; }
    public void setRoomPin(String roomPin) { this.roomPin = roomPin; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
