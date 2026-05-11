package com.classroom.app.web.dto;

import com.classroom.app.domain.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LiveKitTokenRequest(@NotNull Long roomId,
                                  @NotNull ParticipantRole userRole,
                                  @Size(max = 120) String displayName,
                                  @Size(max = 32) String phone,
                                  @Size(max = 16) String pin) {
}
