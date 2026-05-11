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
@PageTitle("Live Class")
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

        preJoinCard.addClassName("surface-card");
        preJoinCard.addClassName("prejoin-card");
        preJoinCard.setPadding(false);
        preJoinCard.setSpacing(false);

        controlBar.addClassName("live-control-bar");
        controlBar.setWidthFull();
        controlBar.getStyle().set("flex-wrap", "wrap");

        add(pageTitle, roomSubtitle, preJoinCard, controlBar, liveStage);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        controlBar.removeAll();
        preJoinCard.removeAll();
        liveStage.removeAll();
        liveStage.setText("");

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
        Paragraph help = new Paragraph("Preview your setup first, then join the classroom when you're ready.");

        Button previewButton = new Button("Start Preview", e -> getElement().executeJs("window.startPreview();"));
        previewButton.addClassName("primary-btn");
        Button joinButton = new Button(role == ParticipantRole.TEACHER ? "Enter Live Class" : "Join Live Class",
                e -> getElement().executeJs("window.joinLiveRoom();"));
        joinButton.addClassName("primary-btn");

        HorizontalLayout actions = new HorizontalLayout(previewButton, joinButton);
        actions.getStyle().set("flex-wrap", "wrap");

        preJoinCard.add(title, help, previewStage, actions);
    }

    private void buildTeacherControls(Long roomId, Teacher teacher) {
        String roomIdValue = String.valueOf(roomId);
        Button muteAll = new Button("Mute All", e -> getElement().executeJs("window.muteAllByTeacher($0);", roomIdValue));
        Button shareScreen = new Button("Share Screen", e -> getElement().executeJs("window.startScreenShare();"));
        Button leave = new Button("Leave Room", e -> {
            getElement().executeJs("window.leaveLiveRoom(false);");
            getUI().ifPresent(ui -> ui.navigate(TeacherDashboardView.class));
        });
        Button close = new Button("Close Class", e -> {
            getElement().executeJs("window.leaveLiveRoom(false);");
            roomService.closeRoom(roomId, teacher);
            Notification.show("Class closed");
            getUI().ifPresent(ui -> ui.navigate(TeacherDashboardView.class));
        });
        controlBar.add(muteAll, shareScreen, leave, close);
    }

    private void buildStudentControls() {
        Button shareScreen = new Button("Share Screen", e -> getElement().executeJs("window.startScreenShare();"));
        Button leave = new Button("Leave Room", e -> {
            getElement().executeJs("window.leaveLiveRoom(true);");
            getUI().ifPresent(ui -> ui.navigate(HomeView.class));
        });
        controlBar.add(shareScreen, leave);
    }
}
