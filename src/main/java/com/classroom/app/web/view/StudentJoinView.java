package com.classroom.app.web.view;

import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.service.JoinValidationResult;
import com.classroom.app.service.RoomService;
import com.classroom.app.service.StudentJoinSessionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Map;

@Route(value = "join/:code", layout = MainLayout.class)
@PageTitle("Join Class - ClassRoom Live")
@AnonymousAllowed
public class StudentJoinView extends VerticalLayout implements BeforeEnterObserver {
    private final RoomService roomService;
    private final StudentJoinSessionService joinSessionService;
    private VirtualRoom room;

    private final TextField name = new TextField("Student Name");
    private final TextField phone = new TextField("Phone (+880...)");
    private final PasswordField pin = new PasswordField("Room PIN");
    private final VerticalLayout content = new VerticalLayout();

    public StudentJoinView(RoomService roomService, StudentJoinSessionService joinSessionService) {
        this.roomService = roomService;
        this.joinSessionService = joinSessionService;
        addClassName("mobile-container");
        addClassName("join-page");
        content.setPadding(false);
        content.setSpacing(false);
        add(content);
    }

    private void renderJoinForm() {
        content.removeAll();
        content.addClassName("join-layout");

        VerticalLayout infoPanel = new VerticalLayout();
        infoPanel.addClassName("surface-card");
        infoPanel.addClassName("info-panel");
        infoPanel.setPadding(false);
        infoPanel.setSpacing(false);
        Span kicker = new Span("Student Entry");
        kicker.addClassName("hero-eyebrow");
        H2 title = new H2("Prepare to join your live classroom.");
        title.addClassName("page-title");
        Paragraph copy = new Paragraph("Enter your details and the room PIN, then continue to a quick device check before you enter the live session.");
        copy.addClassName("hero-copy");
        Div chips = new Div();
        chips.addClassName("chip-row");
        chips.add(infoChip("Class: " + room.getClassroom().getName()));
        chips.add(infoChip("Subject: " + room.getClassroom().getSubject()));
        chips.add(infoChip("Teacher: " + room.getTeacher().getFullName()));
        infoPanel.add(kicker, title, copy, chips);

        VerticalLayout joinCard = new VerticalLayout();
        joinCard.addClassName("surface-card");
        joinCard.addClassName("join-card");
        joinCard.setPadding(false);
        joinCard.setSpacing(false);
        Paragraph formKicker = new Paragraph(statusLabel(room.getStatus()));
        formKicker.addClassName("section-kicker");
        H3 roomTitle = new H3(room.getClassroom().getName());
        roomTitle.addClassName("section-title");
        Paragraph help = new Paragraph("Use the same name and phone number you want the teacher to see inside the live room.");
        help.addClassName("section-copy");

        Button join = new Button("Continue to Device Check", e -> {
            if (name.getValue().trim().isBlank()) {
                Notification.show("Name is required");
                return;
            }
            JoinValidationResult result = roomService.validateStudentJoin(room, pin.getValue(), phone.getValue());
            if (result != JoinValidationResult.OK) {
                Notification.show(message(result));
                return;
            }
            joinSessionService.store(
                    VaadinServletRequest.getCurrent().getHttpServletRequest().getSession(true),
                    room.getId(),
                    room.getRoomCode(),
                    name.getValue(),
                    phone.getValue(),
                    pin.getValue()
            );
            getUI().ifPresent(ui -> ui.navigate(
                    LiveRoomView.class,
                    new RouteParameters(Map.of("role", "student", "roomId", room.getId().toString()))
            ));
        });
        join.addClassName("primary-btn");

        FormLayout form = new FormLayout(name, phone, pin, join);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        joinCard.add(formKicker, roomTitle, help, form, statusBanner(room.getStatus()));

        content.add(infoPanel, joinCard);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String code = event.getRouteParameters().get("code").orElse("");
        room = roomService.findByCode(code).orElse(null);
        content.removeAll();
        content.removeClassName("join-layout");
        if (room == null) {
            VerticalLayout invalidCard = new VerticalLayout();
            invalidCard.addClassName("surface-card");
            invalidCard.addClassName("empty-state");
            invalidCard.setPadding(false);
            invalidCard.setSpacing(false);
            invalidCard.add(new H2("Invalid room link"), new Paragraph("This class link does not exist or is no longer available. Please ask your teacher for a fresh invite link."));
            content.add(invalidCard);
            return;
        }
        renderJoinForm();
    }

    private Div infoChip(String text) {
        Div chip = new Div();
        chip.addClassName("info-chip");
        chip.setText(text);
        return chip;
    }

    private Div statusBanner(RoomStatus status) {
        Div banner = new Div();
        banner.addClassName("status-banner");
        banner.setText(switch (status) {
            case LIVE -> "This class is currently live. Complete your details, then join after the device check.";
            case SCHEDULED -> "This class is prepared but not open yet. You can fill in your details now and wait for the teacher to start.";
            case CLOSED -> "This class has already ended. If this is unexpected, ask the teacher for a new session.";
        });
        return banner;
    }

    private String statusLabel(RoomStatus status) {
        return switch (status) {
            case LIVE -> "Class is live";
            case SCHEDULED -> "Waiting for teacher";
            case CLOSED -> "Class ended";
        };
    }

    private String message(JoinValidationResult result) {
        return switch (result) {
            case CLASS_NOT_STARTED -> "Class not started";
            case CLASS_ENDED -> "Class ended";
            case INVALID_PIN -> "Invalid PIN";
            case INVALID_PHONE -> "Invalid phone format";
            default -> "Join failed";
        };
    }
}
