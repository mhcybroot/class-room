package com.classroom.app.repo;

import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VirtualRoomRepository extends JpaRepository<VirtualRoom, Long> {
    Optional<VirtualRoom> findByRoomCode(String roomCode);
    boolean existsByTeacherAndStatus(Teacher teacher, RoomStatus status);
    List<VirtualRoom> findByTeacher(Teacher teacher);
}
