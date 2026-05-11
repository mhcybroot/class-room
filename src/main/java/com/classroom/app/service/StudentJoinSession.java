package com.classroom.app.service;

import java.io.Serializable;
import java.time.Instant;

public record StudentJoinSession(Long roomId,
                                 String roomCode,
                                 String displayName,
                                 String phone,
                                 String pin,
                                 Instant createdAt) implements Serializable {
}
