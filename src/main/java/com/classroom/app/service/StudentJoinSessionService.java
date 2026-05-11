package com.classroom.app.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class StudentJoinSessionService {
    private static final String SESSION_KEY = StudentJoinSessionService.class.getName() + ".JOIN";
    private static final Duration TTL = Duration.ofMinutes(30);

    public void store(HttpSession session, Long roomId, String roomCode, String displayName, String phone, String pin) {
        if (session == null) {
            return;
        }
        StudentJoinSession joinSession = new StudentJoinSession(
                roomId,
                roomCode,
                displayName.trim(),
                phone.trim(),
                pin.trim(),
                Instant.now()
        );
        session.setAttribute(SESSION_KEY, joinSession);
    }

    public Optional<StudentJoinSession> getValid(HttpSession session, Long roomId) {
        if (session == null) {
            return Optional.empty();
        }
        Object value = session.getAttribute(SESSION_KEY);
        if (!(value instanceof StudentJoinSession joinSession)) {
            return Optional.empty();
        }
        if (!joinSession.roomId().equals(roomId) || isExpired(joinSession)) {
            clear(session);
            return Optional.empty();
        }
        return Optional.of(joinSession);
    }

    public void clear(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
        }
    }

    private boolean isExpired(StudentJoinSession session) {
        return session.createdAt().plus(TTL).isBefore(Instant.now());
    }
}
