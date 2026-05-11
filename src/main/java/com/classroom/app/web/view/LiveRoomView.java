package com.classroom.app.web.view;

import com.classroom.app.domain.ParticipantRole;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.repo.VirtualRoomRepository;
import com.classroom.app.service.RoomService;
import com.classroom.app.service.StudentJoinSession;
import com.classroom.app.service.StudentJoinSessionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@JsModule("livekit-client.js")
@Route(value = "live/:role/:roomId", layout = MainLayout.class)
@PageTitle("Live Class - ClassRoom Live")
@AnonymousAllowed
public class LiveRoomView extends VerticalLayout implements BeforeEnterObserver {
    private final VirtualRoomRepository roomRepository;
    private final TeacherRepository teacherRepository;
    private final RoomService roomService;
    private final StudentJoinSessionService joinSessionService;

    private final H2 pageTitle = new H2("Live Classroom");
    private final Paragraph roomSubtitle = new Paragraph();
    private final Div previewStage = new Div();
    private final Div liveStage = new Div();
    private final VerticalLayout preJoinCard = new VerticalLayout();
    private final HorizontalLayout controlBar = new HorizontalLayout();
    private final Div statusBanner = new Div();
    private final VerticalLayout sidePanel = new VerticalLayout();
    private final VerticalLayout stageShell = new VerticalLayout();

    public LiveRoomView(VirtualRoomRepository roomRepository,
                        TeacherRepository teacherRepository,
                        RoomService roomService,
                        StudentJoinSessionService joinSessionService) {
        this.roomRepository = roomRepository;
        this.teacherRepository = teacherRepository;
        this.roomService = roomService;
        this.joinSessionService = joinSessionService;

        addClassName("mobile-container");
        addClassName("live-room-page");

        pageTitle.addClassName("page-title");
        roomSubtitle.addClassName("meta-line");

        previewStage.setId("lk-preview");
        previewStage.addClassName("preview-stage");
        liveStage.setId("lk-stage");
        liveStage.addClassName("live-stage");

        statusBanner.addClassName("status-banner");
        statusBanner.setId("lk-status-banner");
        statusBanner.setText("Start your preview to begin.");

        preJoinCard.addClassName("surface-card");
        preJoinCard.addClassName("prejoin-card");
        preJoinCard.addClassName("live-card");
        preJoinCard.setPadding(false);
        preJoinCard.setSpacing(false);

        controlBar.addClassName("control-dock");
        controlBar.setWidthFull();

        sidePanel.addClassName("live-side-panel");
        sidePanel.setPadding(false);
        sidePanel.setSpacing(false);

        stageShell.addClassName("surface-card");
        stageShell.addClassName("live-card-shell");
        stageShell.setPadding(false);
        stageShell.setSpacing(false);
        stageShell.add(statusBanner, liveStage, controlBar);

        Div layout = new Div();
        layout.addClassName("live-shell");
        layout.add(stageShell, sidePanel);

        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        controlBar.removeAll();
        preJoinCard.removeAll();
        liveStage.removeAll();
        sidePanel.removeAll();
        liveStage.setText("");
        statusBanner.setText("Start your preview to begin.");

        String roleParam = event.getRouteParameters().get("role").orElse("student");
        Long roomId = Long.valueOf(event.getRouteParameters().get("roomId").orElse("0"));

        VirtualRoom room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            Notification.show("Room not found");
            event.forwardTo(HomeView.class);
            return;
        }

        ParticipantRole role = "teacher".equalsIgnoreCase(roleParam) ? ParticipantRole.TEACHER : ParticipantRole.STUDENT;
        String participantName;

        if (role == ParticipantRole.TEACHER) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                event.forwardTo(LoginView.class);
                return;
            }

            Teacher teacher = teacherRepository.findByEmail(authentication.getName()).orElse(null);
            if (teacher == null) {
                event.forwardTo(LoginView.class);
                return;
            }
            if (!room.getTeacher().getId().equals(teacher.getId())) {
                event.forwardTo(HomeView.class);
                return;
            }
            participantName = teacher.getFullName();
            buildTeacherControls(roomId, teacher);
        } else {
            StudentJoinSession joinSession = joinSessionService.getValid(
                    VaadinServletRequest.getCurrent().getHttpServletRequest().getSession(false),
                    roomId
            ).orElse(null);
            if (joinSession == null) {
                event.forwardTo(StudentJoinView.class, new com.vaadin.flow.router.RouteParameters("code", room.getRoomCode()));
                return;
            }
            participantName = joinSession.displayName();
            buildStudentControls();
        }

        buildPreJoinCard(room, role, participantName);
        buildSidePanel(room, role, participantName);
        String roomIdValue = String.valueOf(roomId);
        getElement().executeJs(
                "window.initLiveRoom($0,$1,$2,$3,$4);",
                roomIdValue,
                role.name(),
                participantName,
                previewStage.getId().orElse("lk-preview"),
                liveStage.getId().orElse("lk-stage")
        );
    }

    private void buildPreJoinCard(VirtualRoom room, ParticipantRole role, String participantName) {
        pageTitle.setText(role == ParticipantRole.TEACHER ? "Teacher Device Check" : "Student Device Check");
        roomSubtitle.setText(room.getClassroom().getName() + " • " + room.getClassroom().getSubject() + " • " + participantName);

        H3 title = new H3(role == ParticipantRole.TEACHER ? "Review devices before you enter" : "Check your camera and microphone");
        title.addClassName("section-title");
        Paragraph help = new Paragraph("Preview your setup first, then join the classroom when you're ready. If no camera is available, the experience will still stay intentional and clear.");
        help.addClassName("section-copy");

        Button previewButton = new Button("Start Preview", e -> getElement().executeJs("window.startPreview();"));
        previewButton.addClassName("primary-btn");
        Button joinButton = new Button(role == ParticipantRole.TEACHER ? "Enter Live Class" : "Join Live Class",
                e -> getElement().executeJs("window.joinLiveRoom();"));
        joinButton.addClassName("secondary-btn");

        HorizontalLayout actions = new HorizontalLayout(previewButton, joinButton);
        actions.addClassName("action-row");

        preJoinCard.add(pageTitle, roomSubtitle, title, help, previewStage, actions);
        sidePanel.add(preJoinCard);
    }

    private void buildSidePanel(VirtualRoom room, ParticipantRole role, String participantName) {
        VerticalLayout info = new VerticalLayout();
        info.addClassName("surface-card");
        info.addClassName("live-status-card");
        info.setPadding(false);
        info.setSpacing(false);

        Paragraph kicker = new Paragraph(role == ParticipantRole.TEACHER ? "Live Teaching Mode" : "Live Learning Mode");
        kicker.addClassName("section-kicker");
        H3 title = new H3(room.getClassroom().getName());
        title.addClassName("section-title");
        Paragraph copy = new Paragraph(role == ParticipantRole.TEACHER
                ? "You can moderate the room, share your screen, and close the class when the session is complete."
                : "You can review your setup, join the session, and share your screen if the class allows it.");
        copy.addClassName("section-copy");

        Div chips = new Div();
        chips.addClassName("chip-row");
        chips.add(infoChip("Role: " + role.name()));
        chips.add(infoChip("Participant: " + participantName));
        chips.add(infoChip("Status: " + room.getStatus().name()));

        info.add(kicker, title, copy, chips);
        sidePanel.add(info);
    }

    private void buildTeacherControls(Long roomId, Teacher teacher) {
        String roomIdValue = String.valueOf(roomId);
        Button muteAll = new Button("Mute All", e -> getElement().executeJs("window.muteAllByTeacher($0);", roomIdValue));
        muteAll.addClassName("ghost-btn");
        Button shareScreen = new Button("Share Screen", e -> getElement().executeJs("window.startScreenShare();"));
        shareScreen.addClassName("secondary-btn");
        Button leave = new Button("Leave Room", e -> {
            getElement().executeJs("window.leaveLiveRoom(false);");
            getUI().ifPresent(ui -> ui.navigate(TeacherLiveView.class));
        });
        leave.addClassName("ghost-btn");
        Button close = new Button("Close Class", e -> {
            getElement().executeJs("window.leaveLiveRoom(false);");
            roomService.closeRoom(roomId, teacher);
            Notification.show("Class closed");
            getUI().ifPresent(ui -> ui.navigate(TeacherLiveView.class));
        });
        close.addClassName("danger-btn");
        controlBar.add(muteAll, shareScreen, leave, close);
    }

    private void buildStudentControls() {
        Button shareScreen = new Button("Share Screen", e -> getElement().executeJs("window.startScreenShare();"));
        shareScreen.addClassName("secondary-btn");
        Button leave = new Button("Leave Room", e -> {
            getElement().executeJs("window.leaveLiveRoom(true);");
            getUI().ifPresent(ui -> ui.navigate(HomeView.class));
        });
        leave.addClassName("ghost-btn");
        controlBar.add(shareScreen, leave);
    }

    private Div infoChip(String text) {
        Div chip = new Div();
        chip.addClassName("info-chip");
        chip.setText(text);
        return chip;
    }
}
