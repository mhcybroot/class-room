package com.classroom.app.service;

import com.classroom.app.domain.*;
import com.classroom.app.repo.ClassroomRepository;
import com.classroom.app.repo.SessionParticipantRepository;
import com.classroom.app.repo.VirtualRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RoomService {
    private final VirtualRoomRepository roomRepository;
    private final ClassroomRepository classroomRepository;
    private final SessionParticipantRepository participantRepository;
    private final LiveKitService liveKitService;

    public RoomService(VirtualRoomRepository roomRepository,
                       ClassroomRepository classroomRepository,
                       SessionParticipantRepository participantRepository,
                       LiveKitService liveKitService) {
        this.roomRepository = roomRepository;
        this.classroomRepository = classroomRepository;
        this.participantRepository = participantRepository;
        this.liveKitService = liveKitService;
    }

    public List<VirtualRoom> roomsByTeacher(Teacher teacher) {
        return roomRepository.findByTeacher(teacher);
    }

    @Transactional
    public VirtualRoom createRoom(Long classroomId, Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher is required");
        }
        Classroom classroom = classroomRepository.findById(classroomId)
                .filter(c -> c.getTeacher().getId().equals(teacher.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));

        VirtualRoom room = new VirtualRoom();
        room.setClassroom(classroom);
        room.setTeacher(teacher);
        room.setRoomCode(VirtualRoom.generateRoomCode());
        room.setRoomPin(String.valueOf(1000 + new Random().nextInt(9000)));
        room.setStatus(RoomStatus.SCHEDULED);
        return roomRepository.save(room);
    }

    @Transactional
    public VirtualRoom openRoom(Long roomId, Teacher teacher) {
        if (roomRepository.existsByTeacherAndStatus(teacher, RoomStatus.LIVE)) {
            throw new IllegalStateException("Teacher already has an active room");
        }
        VirtualRoom room = ownRoom(roomId, teacher);
        room.setStatus(RoomStatus.LIVE);
        VirtualRoom saved = roomRepository.save(room);
        liveKitService.ensureRoomExists(toLiveKitRoomName(saved));
        return saved;
    }

    @Transactional
    public VirtualRoom closeRoom(Long roomId, Teacher teacher) {
        VirtualRoom room = ownRoom(roomId, teacher);
        room.setStatus(RoomStatus.CLOSED);
        VirtualRoom saved = roomRepository.save(room);
        liveKitService.removeAllParticipants(toLiveKitRoomName(saved));
        return saved;
    }

    public Optional<VirtualRoom> findByCode(String code) {
        return roomRepository.findByRoomCode(code);
    }

    public JoinValidationResult validateStudentJoin(VirtualRoom room, String pin, String phone) {
        if (room == null) return JoinValidationResult.ROOM_NOT_FOUND;
        if (room.getStatus() == RoomStatus.SCHEDULED) return JoinValidationResult.CLASS_NOT_STARTED;
        if (room.getStatus() == RoomStatus.CLOSED) return JoinValidationResult.CLASS_ENDED;
        if (!room.getRoomPin().equals(pin)) return JoinValidationResult.INVALID_PIN;
        if (!PhoneValidator.isValid(phone)) return JoinValidationResult.INVALID_PHONE;
        return JoinValidationResult.OK;
    }

    @Transactional
    public void recordParticipant(VirtualRoom room, ParticipantRole role, String displayName, String phone) {
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }
        String normalizedName = displayName == null ? "" : displayName.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
        SessionParticipant p = new SessionParticipant();
        p.setRoom(room);
        p.setRole(role);
        p.setDisplayName(normalizedName);
        p.setPhone(phone == null ? null : phone.trim());
        participantRepository.save(p);
    }

    public int muteAll(Long roomId, Teacher teacher) {
        VirtualRoom room = ownRoom(roomId, teacher);
        return liveKitService.muteAllParticipants(toLiveKitRoomName(room));
    }

    public static String toLiveKitRoomName(VirtualRoom room) {
        return "room-" + room.getRoomCode();
    }

    private VirtualRoom ownRoom(Long roomId, Teacher teacher) {
        return roomRepository.findById(roomId)
                .filter(r -> r.getTeacher().getId().equals(teacher.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }
}
