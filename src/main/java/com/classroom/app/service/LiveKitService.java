package com.classroom.app.service;

import com.classroom.app.config.LiveKitProperties;
import com.classroom.app.domain.ParticipantRole;
import io.livekit.server.*;
import livekit.LivekitModels;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
public class LiveKitService {
    private final LiveKitProperties properties;
    private final RoomServiceClient roomClient;

    public LiveKitService(LiveKitProperties properties) {
        this.properties = properties;
        this.roomClient = RoomServiceClient.create(toHttpUrl(properties.url()), properties.apiKey(), properties.apiSecret());
    }

    public String createToken(String identity, String name, String roomName, ParticipantRole role) {
        if (identity == null || identity.isBlank()) {
            throw new IllegalArgumentException("Identity is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Participant name is required");
        }
        RoomJoin join = new RoomJoin(true);
        AccessToken token = new AccessToken(properties.apiKey(), properties.apiSecret());
        token.setIdentity(identity);
        token.setName(name);
        token.addGrants(join,
                new RoomName(roomName),
                new CanPublish(true),
                new CanSubscribe(true),
                new CanPublishData(true));

        if (role == ParticipantRole.STUDENT) {
            token.addGrants(new CanPublishSources(List.of("camera", "microphone", "screen_share", "screen_share_audio")));
        }
        return token.toJwt();
    }

    public String serverUrl() {
        return properties.url();
    }

    public void ensureRoomExists(String roomName) {
        try {
            Response<LivekitModels.Room> response = roomClient.createRoom(roomName).execute();
            if (!response.isSuccessful() && response.code() != 409) {
                throw new LiveKitOperationException("Could not create LiveKit room");
            }
        } catch (IOException ex) {
            throw new LiveKitOperationException("LiveKit room creation failed");
        }
    }

    public int muteAllParticipants(String roomName) {
        try {
            Response<List<LivekitModels.ParticipantInfo>> response = roomClient.listParticipants(roomName).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new LiveKitOperationException("Could not list room participants");
            }

            int muted = 0;
            for (LivekitModels.ParticipantInfo participant : response.body()) {
                if (participant.getIdentity().startsWith("TEACHER-")) {
                    continue;
                }
                for (LivekitModels.TrackInfo track : participant.getTracksList()) {
                    try {
                        Response<LivekitModels.TrackInfo> muteResponse = roomClient.mutePublishedTrack(
                                roomName,
                                participant.getIdentity(),
                                track.getSid(),
                                true
                        ).execute();
                        if (!muteResponse.isSuccessful()) {
                            throw new LiveKitOperationException("Could not mute participant track");
                        }
                        muted++;
                    } catch (IOException ex) {
                        throw new LiveKitOperationException("LiveKit mute-all request failed");
                    }
                }
            }
            return muted;
        } catch (IOException ex) {
            throw new LiveKitOperationException("Could not reach LiveKit while muting tracks");
        }
    }

    public int removeAllParticipants(String roomName) {
        try {
            Response<List<LivekitModels.ParticipantInfo>> response = roomClient.listParticipants(roomName).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new LiveKitOperationException("Could not list room participants");
            }

            int removed = 0;
            for (LivekitModels.ParticipantInfo participant : response.body()) {
                try {
                    Response<Void> removeResponse = roomClient.removeParticipant(roomName, participant.getIdentity()).execute();
                    if (!removeResponse.isSuccessful() && removeResponse.code() != 404) {
                        throw new LiveKitOperationException("Could not remove participant from room");
                    }
                    removed++;
                } catch (IOException ex) {
                    throw new LiveKitOperationException("LiveKit participant removal failed");
                }
            }
            Response<Void> deleteResponse = roomClient.deleteRoom(roomName).execute();
            if (!deleteResponse.isSuccessful() && deleteResponse.code() != 404) {
                throw new LiveKitOperationException("Could not delete LiveKit room");
            }
            return removed;
        } catch (IOException ex) {
            throw new LiveKitOperationException("Could not reach LiveKit while closing room");
        }
    }

    private static String toHttpUrl(String liveKitUrl) {
        if (liveKitUrl == null) return "http://localhost:7880";
        if (liveKitUrl.startsWith("ws://")) return "http://" + liveKitUrl.substring(5);
        if (liveKitUrl.startsWith("wss://")) return "https://" + liveKitUrl.substring(6);
        return liveKitUrl;
    }
}
