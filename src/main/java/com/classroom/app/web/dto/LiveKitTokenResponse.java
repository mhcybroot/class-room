package com.classroom.app.web.dto;

public record LiveKitTokenResponse(String token, String serverUrl, String roomName, String status) {
}
