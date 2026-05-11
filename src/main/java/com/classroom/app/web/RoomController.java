package com.classroom.app.web;

import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    private final TeacherRepository teacherRepository;

    public RoomController(RoomService roomService, TeacherRepository teacherRepository) {
        this.roomService = roomService;
        this.teacherRepository = teacherRepository;
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<?> open(@PathVariable Long id, Authentication authentication) {
        Teacher teacher = teacherRepository.findByEmail(authentication.getName()).orElseThrow();
        VirtualRoom room = roomService.openRoom(id, teacher);
        return ResponseEntity.ok(Map.of("status", room.getStatus().name()));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id, Authentication authentication) {
        Teacher teacher = teacherRepository.findByEmail(authentication.getName()).orElseThrow();
        VirtualRoom room = roomService.closeRoom(id, teacher);
        return ResponseEntity.ok(Map.of("status", room.getStatus().name(), "disconnected", true));
    }

    @PostMapping("/{id}/mute-all")
    public ResponseEntity<?> muteAll(@PathVariable Long id, Authentication authentication) {
        Teacher teacher = teacherRepository.findByEmail(authentication.getName()).orElseThrow();
        int mutedTracks = roomService.muteAll(id, teacher);
        return ResponseEntity.ok(Map.of("status", "MUTE_ALL_SIGNAL_SENT", "roomId", id, "mutedTracks", mutedTracks));
    }
}
