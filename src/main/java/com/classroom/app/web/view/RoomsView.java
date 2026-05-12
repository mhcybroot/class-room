package com.classroom.app.web.view;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.ClassroomService;
import com.classroom.app.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Route(value = "teacher/rooms", layout = MainLayout.class)
@PageTitle("Manage Rooms - ClassRoom Live")
@RolesAllowed("TEACHER")
public class RoomsView extends VerticalLayout implements BeforeEnterObserver {
    private Teacher teacher;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                event.rerouteTo(LoginView.class);
                return;
        }
    }

    public RoomsView(TeacherRepository teacherRepository, ClassroomService classroomService, RoomService roomService) {
        addClassName("mobile-container");
        addClassName("page-stack");

        var auth = SecurityContextHolder.getContext().getAuthentication();
        this.teacher = teacherRepository.findByEmail(auth.getName()).orElse(null);
        if (this.teacher == null) {
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            return;
        }
        add(buildCreateRoomSection(this.teacher, classroomService, roomService));
        add(buildRoomsSection(this.teacher, roomService));
    }

    private VerticalLayout buildCreateRoomSection(Teacher teacher, ClassroomService classroomService, RoomService roomService) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("card");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Create room"), title("Create room from class"), copy("Choose a class and create a virtual room for it."));

        List<Classroom> classrooms = classroomService.byTeacher(teacher);
        if (classrooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.addClassName("card");
            empty.setPadding(false);
            empty.setSpacing(false);
            empty.add(new H3("No classes available"), copy("Create a class first before creating rooms."));
            section.add(empty);
            return section;
        }

        Div grid = new Div();
        grid.addClassName("room-grid");
        for (Classroom classroom : classrooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);
            card.add(new H3(classroom.getName()), copy("Subject: " + classroom.getSubject()));
            Button createRoom = new Button("Create Room", e -> {
                try {
                    roomService.createRoom(classroom.getId(), teacher);
                    notifySuccess("Room created");
                    getUI().ifPresent(ui -> ui.getPage().reload());
                } catch (RuntimeException ex) {
                    notifyError(ex.getMessage());
                }
            });
            createRoom.addClassName("primary-btn");
            card.add(createRoom);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private VerticalLayout buildRoomsSection(Teacher teacher, RoomService roomService) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("card");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Rooms"), title("Manage rooms"), copy("Open, close, and share invite details from here."));

        List<VirtualRoom> rooms = roomService.roomsByTeacher(teacher);
        if (rooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.addClassName("card");
            empty.setPadding(false);
            empty.setSpacing(false);
            empty.add(new H3("No rooms yet"), copy("Create a room from one of your classes."));
            section.add(empty);
            return section;
        }

        Div grid = new Div();
        grid.addClassName("room-grid");
        for (VirtualRoom room : rooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);

            HorizontalLayout top = new HorizontalLayout(new H3(room.getRoomCode()), statusBadge(room.getStatus().name()));
            top.setWidthFull();
            top.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
            top.addClassName("room-card-head");

            Div meta = new Div();
            meta.addClassName("meta-chip-row");
            meta.add(metaChip("Class", room.getClassroom().getName()));
            meta.add(metaChip("Subject", room.getClassroom().getSubject()));

            Button open = new Button("Open Room", e -> executeRoomAction(() -> roomService.openRoom(room.getId(), teacher), "Room opened"));
            open.addClassName("primary-btn");
            Button close = new Button("Close Room", e -> executeRoomAction(() -> roomService.closeRoom(room.getId(), teacher), "Room closed"));
            close.addClassName("danger-btn");
            Button invite = new Button("Invite Details", e -> inviteDialog(room).open());
            invite.addClassName("secondary-btn");

            HorizontalLayout actions = new HorizontalLayout(open, close, invite);
            actions.addClassName("action-row");
            card.add(top, meta, copy("Manage room status and share the room code and PIN with students."), actions);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private Dialog inviteDialog(VirtualRoom room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Invite Details");

        TextField joinLink = new TextField("Join Link");
        joinLink.setValue(buildAbsoluteJoinLink(room));
        joinLink.setReadOnly(true);
        TextField roomPin = new TextField("Room PIN");
        roomPin.setValue(room.getRoomPin());
        roomPin.setReadOnly(true);

        Button copyLink = new Button("Copy Link", e -> joinLink.getElement().executeJs("navigator.clipboard.writeText($0)", joinLink.getValue()));
        copyLink.addClassName("secondary-btn");
        Button copyPin = new Button("Copy PIN", e -> roomPin.getElement().executeJs("navigator.clipboard.writeText($0)", roomPin.getValue()));
        copyPin.addClassName("secondary-btn");
        Button done = new Button("Done", e -> dialog.close());
        done.addClassName("primary-btn");

        VerticalLayout content = new VerticalLayout(copy("Share these details with students when the room is ready."), joinLink, roomPin, new HorizontalLayout(copyLink, copyPin, done));
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName("card");
        dialog.add(content);
        return dialog;
    }

    private String buildAbsoluteJoinLink(VirtualRoom room) {
        var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        return baseUrl + "/join/" + room.getRoomCode();
    }

    private void executeRoomAction(Runnable action, String successMessage) {
        try {
            action.run();
            notifySuccess(successMessage);
            getUI().ifPresent(ui -> ui.getPage().reload());
        } catch (RuntimeException ex) {
            notifyError(ex.getMessage());
        }
    }

    private Div metaChip(String label, String value) {
        Div chip = new Div();
        chip.addClassName("meta-chip");
        chip.setText(label + ": " + value);
        return chip;
    }

    private Span statusBadge(String text) {
        Span badge = new Span(text);
        badge.addClassName("badge");
        if ("LIVE".equalsIgnoreCase(text)) {
            badge.addClassName("badge-live");
        } else if ("SCHEDULED".equalsIgnoreCase(text)) {
            badge.addClassName("badge-scheduled");
        } else {
            badge.addClassName("badge-closed");
        }
        return badge;
    }

    private Paragraph kicker(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("section-kicker");
        return p;
    }

    private H2 title(String text) {
        H2 h2 = new H2(text);
        h2.addClassName("page-title");
        return h2;
    }

    private Paragraph copy(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("section-copy");
        return p;
    }

    private void notifySuccess(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyError(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
