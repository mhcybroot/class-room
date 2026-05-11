package com.classroom.app.web;

import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {
    @Mock
    private RoomService roomService;
    @Mock
    private TeacherRepository teacherRepository;

    private RoomController controller;

    @BeforeEach
    void setUp() {
        controller = new RoomController(roomService, teacherRepository);
    }

    @Test
    void shouldReturnMuteAllPayloadForTeacher() {
        Teacher teacher = new Teacher();
        teacher.setEmail("teacher@example.com");
        when(teacherRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
        when(roomService.muteAll(10L, teacher)).thenReturn(4);

        var response = controller.muteAll(10L, new TestingAuthenticationToken("teacher@example.com", "pw"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, ((Map<?, ?>) response.getBody()).get("mutedTracks"));
    }
}
