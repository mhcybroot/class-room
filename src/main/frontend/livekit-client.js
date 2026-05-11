import { Room, RoomEvent, createLocalTracks } from 'livekit-client';

window.lk = {
  room: null,
  config: null,
  previewTracks: [],
  trackNodes: new Map(),
  joined: false,
  previewSummary: 'No preview started yet.',
};

function previewHost() {
  return document.getElementById(window.lk.config?.previewStageId ?? '');
}

function liveHost() {
  return document.getElementById(window.lk.config?.liveStageId ?? '');
}

function statusHost() {
  return document.getElementById('lk-status-banner');
}

function renderStatus(message) {
  const banner = statusHost();
  if (banner) {
    banner.textContent = message;
  }

  const host = liveHost();
  if (!host) return;
  let status = host.querySelector('[data-status]');
  if (!status) {
    status = document.createElement('div');
    status.setAttribute('data-status', 'true');
    status.className = 'status-copy';
    status.style.padding = '0.75rem 0.95rem';
    host.prepend(status);
  }
  status.textContent = message;
}

function roomGrid() {
  const host = liveHost();
  if (!host) return null;
  let grid = host.querySelector('[data-grid]');
  if (!grid) {
    grid = document.createElement('div');
    grid.setAttribute('data-grid', 'true');
    grid.style.display = 'grid';
    grid.style.gridTemplateColumns = 'repeat(auto-fit, minmax(220px, 1fr))';
    grid.style.gap = '0.9rem';
    grid.style.padding = '0.9rem';
    host.appendChild(grid);
  }
  return grid;
}

function trackKey(participantIdentity, trackSid) {
  return `${participantIdentity}:${trackSid}`;
}

function trackCard(identity, mediaElement) {
  const card = document.createElement('div');
  card.style.border = '1px solid rgba(21, 44, 36, 0.08)';
  card.style.borderRadius = '18px';
  card.style.padding = '0.55rem';
  card.style.background = 'rgba(255, 253, 249, 0.96)';
  card.style.boxShadow = '0 14px 30px rgba(28, 40, 36, 0.08)';

  const label = document.createElement('div');
  label.textContent = identity;
  label.style.fontSize = '12px';
  label.style.fontWeight = '800';
  label.style.letterSpacing = '0.08em';
  label.style.textTransform = 'uppercase';
  label.style.padding = '0.35rem 0.25rem 0.55rem';
  label.style.color = '#20332d';

  mediaElement.style.width = '100%';
  mediaElement.style.borderRadius = '14px';
  mediaElement.style.background = '#10261f';

  card.appendChild(label);
  card.appendChild(mediaElement);
  return card;
}

function attachTrack(identity, track) {
  const grid = roomGrid();
  if (!grid) return;
  const key = trackKey(identity, track.sid ?? identity);
  if (window.lk.trackNodes.has(key)) return;
  const element = track.attach();
  const card = trackCard(identity, element);
  window.lk.trackNodes.set(key, card);
  grid.appendChild(card);
}

function detachTrack(identity, track) {
  const key = trackKey(identity, track.sid ?? identity);
  const card = window.lk.trackNodes.get(key);
  if (card) {
    card.remove();
    window.lk.trackNodes.delete(key);
  }
  track.detach().forEach((el) => el.remove());
}

async function ensurePreviewTracks() {
  if (window.lk.previewTracks.length > 0) {
    return window.lk.previewTracks;
  }

  if (!window.isSecureContext || !navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) {
    window.lk.previewTracks = [];
    window.lk.previewSummary = 'Camera/microphone require HTTPS (or localhost). Open this page over https:// to use local media.';
    return window.lk.previewTracks;
  }

  const devices = await navigator.mediaDevices.enumerateDevices();
  const hasAudioInput = devices.some((device) => device.kind === 'audioinput');
  const hasVideoInput = devices.some((device) => device.kind === 'videoinput');

  if (!hasAudioInput && !hasVideoInput) {
    window.lk.previewTracks = [];
    window.lk.previewSummary = 'No camera or microphone found. You can still enter the class without local media.';
    return window.lk.previewTracks;
  }

  try {
    window.lk.previewTracks = await createLocalTracks({
      audio: hasAudioInput,
      video: hasVideoInput
    });
    const parts = [];
    if (hasVideoInput) parts.push('camera');
    if (hasAudioInput) parts.push('microphone');
    window.lk.previewSummary = `${parts.join(' and ')} ready.`;
    return window.lk.previewTracks;
  } catch (error) {
    if (hasVideoInput) {
      try {
        window.lk.previewTracks = await createLocalTracks({ audio: false, video: true });
        window.lk.previewSummary = 'Camera ready. Microphone unavailable.';
        return window.lk.previewTracks;
      } catch (videoError) {
        // Try audio only next.
      }
    }

    if (hasAudioInput) {
      try {
        window.lk.previewTracks = await createLocalTracks({ audio: true, video: false });
        window.lk.previewSummary = 'Microphone ready. Camera unavailable.';
        return window.lk.previewTracks;
      } catch (audioError) {
        // Fall through to no local media.
      }
    }

    window.lk.previewTracks = [];
    window.lk.previewSummary = `Local media unavailable: ${error.message}`;
  }

  return window.lk.previewTracks;
}

function renderPreview() {
  const host = previewHost();
  if (!host) return;
  host.innerHTML = '';

  const frame = document.createElement('div');
  frame.style.width = '100%';
  frame.style.minHeight = '280px';
  frame.style.display = 'flex';
  frame.style.alignItems = 'center';
  frame.style.justifyContent = 'center';
  frame.style.position = 'relative';
  frame.style.padding = '0.9rem';
  frame.style.boxSizing = 'border-box';

  const hint = document.createElement('div');
  hint.textContent = window.lk.previewSummary;
  hint.style.position = 'absolute';
  hint.style.left = '1rem';
  hint.style.bottom = '1rem';
  hint.style.padding = '0.65rem 0.85rem';
  hint.style.borderRadius = '999px';
  hint.style.background = 'rgba(255, 253, 249, 0.92)';
  hint.style.color = '#20332d';
  hint.style.fontWeight = '700';
  hint.style.fontSize = '0.9rem';

  if (window.lk.previewTracks.length === 0) {
    const empty = document.createElement('div');
    empty.style.padding = '1.2rem';
    empty.style.textAlign = 'center';
    empty.style.maxWidth = '26rem';
    empty.style.color = '#5f716b';
    empty.textContent = window.lk.previewSummary;
    frame.appendChild(empty);
    host.appendChild(frame);
    return;
  }

  const videoTrack = window.lk.previewTracks.find((track) => track.kind === 'video');
  if (videoTrack) {
    const element = videoTrack.attach();
    element.style.width = '100%';
    element.style.height = '100%';
    element.style.maxHeight = '320px';
    element.style.objectFit = 'cover';
    element.style.borderRadius = '18px';
    frame.appendChild(element);
  } else {
    const placeholder = document.createElement('div');
    placeholder.style.width = '100%';
    placeholder.style.minHeight = '220px';
    placeholder.style.display = 'flex';
    placeholder.style.alignItems = 'center';
    placeholder.style.justifyContent = 'center';
    placeholder.style.textAlign = 'center';
    placeholder.style.padding = '1.2rem';
    placeholder.style.color = '#5f716b';
    placeholder.textContent = 'Camera preview is unavailable, but your microphone can still join the class.';
    frame.appendChild(placeholder);
  }

  frame.appendChild(hint);
  host.appendChild(frame);
}

async function publishPreviewTracks(room) {
  const tracks = await ensurePreviewTracks();
  for (const track of tracks) {
    await room.localParticipant.publishTrack(track);
    if (track.kind === 'video') {
      attachTrack('You', track);
    }
  }
}

function bindRoomEvents(room) {
  room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
    attachTrack(participant.identity, track);
  });

  room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
    detachTrack(participant.identity, track);
  });

  room.on(RoomEvent.ParticipantConnected, (participant) => {
    renderStatus(`${participant.identity} joined the room`);
  });

  room.on(RoomEvent.ParticipantDisconnected, (participant) => {
    renderStatus(`${participant.identity} left the room`);
  });

  room.on(RoomEvent.Disconnected, async () => {
    window.lk.joined = false;
    renderStatus('Disconnected from class');
    if (window.lk.config?.role === 'STUDENT') {
      await fetch('/api/livekit/leave', { method: 'POST' });
    }
  });
}

window.initLiveRoom = function(roomId, role, displayName, previewStageId, liveStageId) {
  window.lk.previewTracks.forEach((track) => track.stop());
  window.lk.config = {
    roomId: String(roomId),
    role,
    displayName,
    previewStageId,
    liveStageId
  };
  window.lk.previewTracks = [];
  window.lk.previewSummary = 'No preview started yet.';
  window.lk.trackNodes.clear();
  const live = liveHost();
  if (live) {
    live.innerHTML = '';
  }
  renderStatus('Start preview, then join when your device is ready.');
};

window.startPreview = async function() {
  try {
    await ensurePreviewTracks();
    renderPreview();
    renderStatus(window.lk.previewSummary);
  } catch (error) {
    renderStatus(`Preview failed: ${error.message}`);
  }
};

window.joinLiveRoom = async function() {
  try {
    await ensurePreviewTracks();
    const response = await fetch('/api/livekit/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roomId: Number(window.lk.config.roomId),
        userRole: window.lk.config.role
      })
    });

    const payload = await response.json();
    if (!response.ok) {
      renderStatus(payload.message ?? `Join failed: ${payload.status ?? payload.code}`);
      return;
    }

    const room = new Room({
      adaptiveStream: true,
      dynacast: true,
      videoCaptureDefaults: { resolution: { width: 640, height: 360 } }
    });

    bindRoomEvents(room);
    window.lk.room = room;

    await room.connect(payload.serverUrl, payload.token);
    await publishPreviewTracks(room);
    window.lk.joined = true;

    for (const participant of room.remoteParticipants.values()) {
      participant.trackPublications.forEach((publication) => {
        if (publication.track) {
          attachTrack(participant.identity, publication.track);
        }
      });
    }

    renderStatus(`Connected to ${payload.roomName}`);
  } catch (error) {
    renderStatus(`Connection error: ${error.message}`);
  }
};

window.startScreenShare = async function() {
  if (!window.lk.room) return;
  try {
    await window.lk.room.localParticipant.setScreenShareEnabled(true);
    renderStatus('Screen sharing started');
  } catch (error) {
    renderStatus(`Screen sharing failed: ${error.message}`);
  }
};

window.muteAllByTeacher = async function(roomId) {
  const response = await fetch(`/api/rooms/${roomId}/mute-all`, { method: 'POST' });
  const payload = await response.json();
  if (!response.ok) {
    renderStatus(payload.message ?? 'Mute all failed');
    return payload;
  }
  renderStatus(`Mute all completed. ${payload.mutedTracks ?? 0} tracks muted.`);
  return payload;
};

window.leaveLiveRoom = async function(clearStudentSession) {
  if (window.lk.room) {
    await window.lk.room.disconnect();
    window.lk.room = null;
  }
  window.lk.previewTracks.forEach((track) => track.stop());
  window.lk.previewTracks = [];
  window.lk.joined = false;
  if (clearStudentSession) {
    await fetch('/api/livekit/leave', { method: 'POST' });
  }
  renderStatus('You left the live room');
};
