package com.classroom.app.repo;

import com.classroom.app.domain.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
}
