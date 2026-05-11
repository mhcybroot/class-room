package com.classroom.app.web;

import com.classroom.app.domain.ParticipantRole;
import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.repo.VirtualRoomRepository;
import com.classroom.app.service.LiveKitService;
import com.classroom.app.service.JoinValidationResult;
import com.classroom.app.service.RoomService;
import com.classroom.app.service.StudentJoinSessionService;
import com.classroom.app.web.dto.ApiErrorResponse;
import com.classroom.app.web.dto.LiveKitTokenRequest;
import com.classroom.app.web.dto.LiveKitTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveKitControllerTest {
    @Mock
    private LiveKitService liveKitService;
    @Mock
    private RoomService roomService;
    @Mock
    private VirtualRoomRepository roomRepository;
    @Mock
    private TeacherRepository teacherRepository;

    private StudentJoinSessionService joinSessionService;
    private LiveKitController controller;

    @BeforeEach
    void setUp() {
        joinSessionService = new StudentJoinSessionService();
        controller = new LiveKitController(liveKitService, roomService, roomRepository, teacherRepository, joinSessionService);
    }

    @Test
    void shouldRejectAnonymousTeacherTokenRequest() throws Exception {
        VirtualRoom room = room(1L, 50L, RoomStatus.LIVE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(SecurityException.class, () -> controller.token(
                new LiveKitTokenRequest(1L, ParticipantRole.TEACHER, null, null, null),
                null,
                new MockHttpSession()
        ));
    }

    @Test
    void shouldMintStudentTokenFromSessionState() throws Exception {
        VirtualRoom room = room(1L, 50L, RoomStatus.LIVE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(liveKitService.serverUrl()).thenReturn("ws://localhost:7880");
        when(liveKitService.createToken(anyString(), eq("Student A"), anyString(), eq(ParticipantRole.STUDENT)))
                .thenReturn("student-token");
        when(roomService.validateStudentJoin(eq(room), eq("1234"), eq("+8801712345678")))
                .thenReturn(JoinValidationResult.OK);

        MockHttpSession session = new MockHttpSession();
        joinSessionService.store(session, 1L, "abc12345", "Student A", "+8801712345678", "1234");

        var response = controller.token(
                new LiveKitTokenRequest(1L, ParticipantRole.STUDENT, null, null, null),
                null,
                session
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LiveKitTokenResponse body = (LiveKitTokenResponse) response.getBody();
        assertEquals("student-token", body.token());
        verify(roomService).recordParticipant(eq(room), eq(ParticipantRole.STUDENT), eq("Student A"), eq("+8801712345678"));
    }

    @Test
    void shouldRejectMissingStudentJoinSession() throws Exception {
        VirtualRoom room = room(1L, 50L, RoomStatus.LIVE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        var response = controller.token(
                new LiveKitTokenRequest(1L, ParticipantRole.STUDENT, null, null, null),
                null,
                new MockHttpSession()
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(ApiErrorResponse.class, response.getBody());
    }

    @Test
    void shouldAllowOwnerTeacherTokenRequest() throws Exception {
        VirtualRoom room = room(1L, 50L, RoomStatus.LIVE);
        Teacher teacher = teacher(50L, "teacher@example.com", "Owner Teacher");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(teacherRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
        when(liveKitService.serverUrl()).thenReturn("ws://localhost:7880");
        when(liveKitService.createToken(anyString(), eq("Owner Teacher"), anyString(), eq(ParticipantRole.TEACHER)))
                .thenReturn("teacher-token");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("teacher@example.com", "pw");
        authentication.setAuthenticated(true);

        var response = controller.token(
                new LiveKitTokenRequest(1L, ParticipantRole.TEACHER, null, null, null),
                authentication,
                new MockHttpSession()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LiveKitTokenResponse body = (LiveKitTokenResponse) response.getBody();
        assertEquals("teacher-token", body.token());
    }

    private static VirtualRoom room(Long roomId, Long teacherId, RoomStatus status) throws Exception {
        Teacher teacher = teacher(teacherId, "teacher@example.com", "Owner Teacher");
        VirtualRoom room = new VirtualRoom();
        setField(room, "id", roomId);
        room.setTeacher(teacher);
        room.setRoomCode("abc12345");
        room.setRoomPin("1234");
        room.setStatus(status);
        return room;
    }

    private static Teacher teacher(Long id, String email, String fullName) throws Exception {
        Teacher teacher = new Teacher();
        setField(teacher, "id", id);
        teacher.setEmail(email);
        teacher.setFullName(fullName);
        teacher.setRole("TEACHER");
        teacher.setPasswordHash("hash");
        return teacher;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
