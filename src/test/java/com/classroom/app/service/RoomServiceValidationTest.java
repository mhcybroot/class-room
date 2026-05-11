package com.classroom.app.service;

import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.VirtualRoom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomServiceValidationTest {
    @Test
    void shouldHandleRoomStateAndPinValidation() {
        VirtualRoom room = new VirtualRoom();
        room.setRoomPin("1234");

        room.setStatus(RoomStatus.SCHEDULED);
        assertEquals(JoinValidationResult.CLASS_NOT_STARTED, evaluate(room, "1234", "+8801712345678"));

        room.setStatus(RoomStatus.CLOSED);
        assertEquals(JoinValidationResult.CLASS_ENDED, evaluate(room, "1234", "+8801712345678"));

        room.setStatus(RoomStatus.LIVE);
        assertEquals(JoinValidationResult.INVALID_PIN, evaluate(room, "0000", "+8801712345678"));
        assertEquals(JoinValidationResult.INVALID_PHONE, evaluate(room, "1234", "bad"));
        assertEquals(JoinValidationResult.OK, evaluate(room, "1234", "+8801712345678"));
    }

    private JoinValidationResult evaluate(VirtualRoom room, String pin, String phone) {
        if (room.getStatus() == RoomStatus.SCHEDULED) return JoinValidationResult.CLASS_NOT_STARTED;
        if (room.getStatus() == RoomStatus.CLOSED) return JoinValidationResult.CLASS_ENDED;
        if (!room.getRoomPin().equals(pin)) return JoinValidationResult.INVALID_PIN;
        if (!PhoneValidator.isValid(phone)) return JoinValidationResult.INVALID_PHONE;
        return JoinValidationResult.OK;
    }
}
