package com.classroom.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class StudentJoinSessionServiceTest {
    private final StudentJoinSessionService service = new StudentJoinSessionService();

    @Test
    void shouldStoreAndReadValidJoinSession() {
        MockHttpSession session = new MockHttpSession();

        service.store(session, 10L, "room-code", "Student A", "+8801712345678", "1234");

        StudentJoinSession result = service.getValid(session, 10L).orElseThrow();
        assertEquals("Student A", result.displayName());
        assertEquals("+8801712345678", result.phone());
    }

    @Test
    void shouldClearJoinSession() {
        MockHttpSession session = new MockHttpSession();
        service.store(session, 10L, "room-code", "Student A", "+8801712345678", "1234");

        service.clear(session);

        assertTrue(service.getValid(session, 10L).isEmpty());
    }
}
