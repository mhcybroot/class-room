package com.classroom.app.web;

import com.classroom.app.domain.ParticipantRole;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.repo.VirtualRoomRepository;
import com.classroom.app.service.JoinValidationResult;
import com.classroom.app.service.LiveKitService;
import com.classroom.app.service.RoomService;
import com.classroom.app.service.StudentJoinSession;
import com.classroom.app.service.StudentJoinSessionService;
import com.classroom.app.web.dto.ApiErrorResponse;
import com.classroom.app.web.dto.LiveKitTokenRequest;
import com.classroom.app.web.dto.LiveKitTokenResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livekit")
public class LiveKitController {
    private final LiveKitService liveKitService;
    private final RoomService roomService;
    private final VirtualRoomRepository roomRepository;
    private final TeacherRepository teacherRepository;
    private final StudentJoinSessionService studentJoinSessionService;

    public LiveKitController(LiveKitService liveKitService,
                             RoomService roomService,
                             VirtualRoomRepository roomRepository,
                             TeacherRepository teacherRepository,
                             StudentJoinSessionService studentJoinSessionService) {
        this.liveKitService = liveKitService;
        this.roomService = roomService;
        this.roomRepository = roomRepository;
        this.teacherRepository = teacherRepository;
        this.studentJoinSessionService = studentJoinSessionService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@Valid @RequestBody LiveKitTokenRequest request,
                                   Authentication authentication,
                                   HttpSession session) {
        VirtualRoom room = roomRepository.findById(request.roomId()).orElse(null);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LiveKitTokenResponse(null, null, null, JoinValidationResult.ROOM_NOT_FOUND.name()));
        }

        if (request.userRole() == ParticipantRole.STUDENT) {
            StudentJoinSession joinSession = studentJoinSessionService.getValid(session, room.getId()).orElse(null);
            if (joinSession == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiErrorResponse("INVALID_JOIN_STATE", "Your join session expired. Please join again."));
            }
            JoinValidationResult result = roomService.validateStudentJoin(room, joinSession.pin(), joinSession.phone());
            if (result != JoinValidationResult.OK) {
                studentJoinSessionService.clear(session);
                return ResponseEntity.badRequest().body(new LiveKitTokenResponse(null, null, null, result.name()));
            }
            roomService.recordParticipant(room, ParticipantRole.STUDENT, joinSession.displayName(), joinSession.phone());

            String roomName = RoomService.toLiveKitRoomName(room);
            String token = liveKitService.createToken(
                    request.userRole() + "-" + joinSession.displayName(),
                    joinSession.displayName(),
                    roomName,
                    request.userRole()
            );
            return ResponseEntity.ok(new LiveKitTokenResponse(token, liveKitService.serverUrl(), roomName, "OK"));
        }

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new SecurityException("Teacher authentication is required");
        }
        Teacher teacher = teacherRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Teacher account not found"));
        if (!room.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("You do not have access to this room");
        }
        if (room.getStatus() != com.classroom.app.domain.RoomStatus.LIVE) {
            throw new IllegalStateException("Room must be open before joining live class");
        }
        roomService.recordParticipant(room, ParticipantRole.TEACHER, teacher.getFullName(), null);
        String roomName = RoomService.toLiveKitRoomName(room);
        String token = liveKitService.createToken(
                "TEACHER-" + teacher.getId(),
                teacher.getFullName(),
                roomName,
                request.userRole()
        );
        return ResponseEntity.ok(new LiveKitTokenResponse(token, liveKitService.serverUrl(), roomName, "OK"));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leave(HttpSession session) {
        studentJoinSessionService.clear(session);
        return ResponseEntity.ok().build();
    }
}
