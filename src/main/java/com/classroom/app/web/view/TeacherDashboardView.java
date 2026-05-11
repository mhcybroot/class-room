package com.classroom.app.web.view;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.ClassroomService;
import com.classroom.app.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@Route(value = "teacher", layout = MainLayout.class)
@PageTitle("Teacher Dashboard")
@RolesAllowed("TEACHER")
public class TeacherDashboardView extends VerticalLayout {
    public TeacherDashboardView(TeacherRepository teacherRepository, ClassroomService classroomService, RoomService roomService) {
        addClassName("mobile-container");
        addClassName("teacher-dashboard-page");

        Teacher teacher = teacherRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();

        H2 title = new H2("Teacher Dashboard");
        title.addClassName("page-title");
        add(title, new Paragraph("Manage classes, open live rooms, and share classroom invite details from one mobile-friendly screen."));

        add(buildCreateClassCard(teacher, classroomService));
        add(buildClassesSection(teacher, classroomService, roomService));
        add(buildRoomsSection(teacher, roomService));
    }

    private VerticalLayout buildCreateClassCard(Teacher teacher, ClassroomService classroomService) {
        TextField className = new TextField("Class Name");
        TextField subject = new TextField("Subject");
        Button addClass = new Button("Create Class", e -> {
            try {
                classroomService.create(teacher, className.getValue(), subject.getValue());
                notifySuccess("Class created");
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (RuntimeException ex) {
                notifyError(ex.getMessage());
            }
        });
        addClass.addClassName("primary-btn");

        FormLayout form = new FormLayout(className, subject, addClass);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout card = new VerticalLayout(new H3("Create a new class"), new Paragraph("Add the subject and publish a room when you are ready to teach."), form);
        card.addClassName("surface-card");
        card.setPadding(false);
        card.setSpacing(false);
        return card;
    }

    private VerticalLayout buildClassesSection(Teacher teacher, ClassroomService classroomService, RoomService roomService) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.add(new H3("Your Classes"));

        List<Classroom> classrooms = classroomService.byTeacher(teacher);
        if (classrooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout(new Paragraph("No classes yet. Create your first class to start inviting students."));
            empty.addClassName("surface-card");
            empty.addClassName("empty-state");
            empty.setPadding(false);
            empty.setSpacing(false);
            section.add(empty);
            return section;
        }

        Div grid = new Div();
        grid.addClassName("room-grid");
        for (Classroom classroom : classrooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("surface-card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);

            Button createRoom = new Button("Create Room", e -> {
                try {
                    roomService.createRoom(classroom.getId(), teacher);
                    notifySuccess("Virtual room created");
                    getUI().ifPresent(ui -> ui.getPage().reload());
                } catch (RuntimeException ex) {
                    notifyError(ex.getMessage());
                }
            });
            Button delete = new Button("Delete Class", e -> {
                classroomService.deleteForTeacher(classroom.getId(), teacher);
                notifySuccess("Class deleted");
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            HorizontalLayout actions = new HorizontalLayout(createRoom, delete);
            actions.getStyle().set("flex-wrap", "wrap");

            card.add(new H3(classroom.getName()), new Paragraph("Subject: " + classroom.getSubject()), actions);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private VerticalLayout buildRoomsSection(Teacher teacher, RoomService roomService) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.add(new H3("Virtual Rooms"));

        List<VirtualRoom> rooms = roomService.roomsByTeacher(teacher);
        if (rooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout(new Paragraph("No virtual rooms yet. Create one from a class card above."));
            empty.addClassName("surface-card");
            empty.addClassName("empty-state");
            empty.setPadding(false);
            empty.setSpacing(false);
            section.add(empty);
            return section;
        }

        Div grid = new Div();
        grid.addClassName("room-grid");
        for (VirtualRoom room : rooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("surface-card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);

            Paragraph details = new Paragraph("Class: " + room.getClassroom().getName() + " • Status: " + room.getStatus());
            Button open = new Button("Open Room", e -> executeRoomAction(() -> roomService.openRoom(room.getId(), teacher), "Room is live"));
            Button close = new Button("Close Room", e -> executeRoomAction(() -> roomService.closeRoom(room.getId(), teacher), "Room closed"));
            Button live = new Button("Device Check & Enter", e -> getUI().ifPresent(ui -> ui.navigate(
                    LiveRoomView.class,
                    new RouteParameters(Map.of("role", "teacher", "roomId", room.getId().toString()))
            )));
            Button invite = new Button("Invite Details", e -> buildInviteDialog(room).open());

            HorizontalLayout actions = new HorizontalLayout(open, close, live, invite);
            actions.getStyle().set("flex-wrap", "wrap");

            card.add(new H3("Room " + room.getRoomCode()), details, actions);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private Dialog buildInviteDialog(VirtualRoom room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Invite Details");

        TextField joinLink = new TextField("Join Link");
        joinLink.setValue(buildAbsoluteJoinLink(room));
        joinLink.setReadOnly(true);

        TextField roomPin = new TextField("Room PIN");
        roomPin.setValue(room.getRoomPin());
        roomPin.setReadOnly(true);

        Button close = new Button("Done", e -> dialog.close());
        Button copyLink = new Button("Copy Link", e -> joinLink.getElement().executeJs("navigator.clipboard.writeText($0)", joinLink.getValue()));
        Button copyPin = new Button("Copy PIN", e -> roomPin.getElement().executeJs("navigator.clipboard.writeText($0)", roomPin.getValue()));

        VerticalLayout layout = new VerticalLayout(
                new Paragraph("Share the join link and PIN with your students when the room is ready."),
                joinLink,
                roomPin,
                new HorizontalLayout(copyLink, copyPin, close)
        );
        dialog.add(layout);
        return dialog;
    }

    private String buildAbsoluteJoinLink(VirtualRoom room) {
        var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), request.getContextPath());
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

    private void notifySuccess(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyError(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
