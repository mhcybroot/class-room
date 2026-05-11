package com.classroom.app.web.view;

import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.service.JoinValidationResult;
import com.classroom.app.service.RoomService;
import com.classroom.app.service.StudentJoinSessionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
@PageTitle("Join Class")
@AnonymousAllowed
public class StudentJoinView extends VerticalLayout implements BeforeEnterObserver {
    private final RoomService roomService;
    private final StudentJoinSessionService joinSessionService;
    private VirtualRoom room;

    private final TextField name = new TextField("Name");
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
        H2 title = new H2("Student Join");
        title.addClassName("page-title");
        H3 roomTitle = new H3(room.getClassroom().getName());
        Paragraph help = new Paragraph("Enter your details and continue to the device check before joining the live class.");

        Button join = new Button("Continue", e -> {
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
        VerticalLayout card = new VerticalLayout(title, roomTitle, help, form);
        card.addClassName("surface-card");
        card.setPadding(false);
        card.setSpacing(false);
        content.add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String code = event.getRouteParameters().get("code").orElse("");
        room = roomService.findByCode(code).orElse(null);
        if (room == null) {
            content.removeAll();
            VerticalLayout invalidCard = new VerticalLayout(new H2("Invalid room link"), new Paragraph("This class link does not exist or is no longer available."));
            invalidCard.addClassName("surface-card");
            invalidCard.setPadding(false);
            invalidCard.setSpacing(false);
            content.add(invalidCard);
            return;
        }
        renderJoinForm();
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
