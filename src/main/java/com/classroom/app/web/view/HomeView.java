package com.classroom.app.web.view;

import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.VirtualRoomRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("ClassRoom Live - Virtual Classroom")
@AnonymousAllowed
public class HomeView extends VerticalLayout {
    public HomeView(VirtualRoomRepository roomRepository) {
        addClassName("mobile-container");
        addClassName("page-stack");

        List<VirtualRoom> rooms = roomRepository.findAll();

        add(buildHero());
        add(buildMeetingsSection(rooms));
        add(buildJoinByCodeSection());
        add(buildTeacherPromptSection());
    }

    private VerticalLayout buildHero() {
        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("hero-panel");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(
                kicker("Home"),
                title("Join a meeting or manage your classroom."),
                copy("Students can join with a room code. Teachers can log in to manage classes, rooms, and live sessions.")
        );
        return hero;
    }

    private VerticalLayout buildMeetingsSection(List<VirtualRoom> rooms) {
        VerticalLayout section = section("Available meetings", "All available meetings", "See the current meeting list and join open rooms.");

        if (rooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.addClassName("surface-card");
            empty.addClassName("empty-state");
            empty.setPadding(false);
            empty.setSpacing(false);
            empty.add(sectionTitle("No meetings available"), copy("When a teacher creates and opens a room, it will appear here."));
            section.add(empty);
            return section;
        }

        Div roomGrid = new Div();
        roomGrid.addClassName("room-grid");
        roomGrid.setWidthFull();
        for (VirtualRoom room : rooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("surface-card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);
            card.setWidthFull();

            H3 className = new H3(room.getClassroom().getName());
            className.addClassName("room-title");
            SpanBadge badge = statusBadge(room.getStatus());

            HorizontalLayout top = new HorizontalLayout(className, badge);
            top.setWidthFull();
            top.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
            top.addClassName("room-card-head");

            Div meta = new Div();
            meta.addClassName("meta-chip-row");
            meta.add(metaChip("Teacher", room.getTeacher().getFullName()));
            meta.add(metaChip("Subject", room.getClassroom().getSubject()));
            meta.add(metaChip("Room Code", room.getRoomCode()));

            Button join = new Button(room.getStatus() == RoomStatus.LIVE ? "Join Meeting" : room.getStatus() == RoomStatus.SCHEDULED ? "Not Started" : "Closed");
            join.setEnabled(room.getStatus() == RoomStatus.LIVE);
            join.addClassName(room.getStatus() == RoomStatus.LIVE ? "primary-btn" : "secondary-btn");
            join.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("join/" + room.getRoomCode())));

            card.add(top, meta, copy(description(room.getStatus())), join);
            roomGrid.add(card);
        }

        section.add(roomGrid);
        return section;
    }

    private VerticalLayout buildJoinByCodeSection() {
        VerticalLayout section = section("Join with room code", "Enter room code", "Use the room code shared by your teacher.");
        TextField roomCodeInput = new TextField("Room Code");
        roomCodeInput.setPlaceholder("Example: ab12cd34");
        Button goJoin = new Button("Continue", e -> {
            String code = roomCodeInput.getValue() == null ? "" : roomCodeInput.getValue().trim();
            if (!code.isBlank()) {
                getUI().ifPresent(ui -> ui.navigate("join/" + code));
            }
        });
        goJoin.addClassName("primary-btn");
        section.add(roomCodeInput, goJoin);
        return section;
    }

    private VerticalLayout buildTeacherPromptSection() {
        VerticalLayout section = section("Teacher access", "Are you a teacher?", "Log in to create classes, manage rooms, and access live session controls.");
        Button loginButton = new Button("Teacher Login", e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
        loginButton.addClassName("primary-btn");
        section.add(loginButton);
        return section;
    }

    private VerticalLayout section(String kicker, String title, String copy) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-band");
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        section.add(kicker(kicker), sectionTitle(title), copy(copy));
        return section;
    }

    private Div metaChip(String label, String value) {
        Div chip = new Div();
        chip.addClassName("meta-chip");
        chip.setText(label + ": " + value);
        return chip;
    }

    private Paragraph kicker(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("section-kicker");
        return p;
    }

    private H1 title(String text) {
        H1 h1 = new H1(text);
        h1.addClassName("hero-title");
        return h1;
    }

    private H3 sectionTitle(String text) {
        H3 h3 = new H3(text);
        h3.addClassName("section-title");
        return h3;
    }

    private Paragraph copy(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("section-copy");
        return p;
    }

    private String description(RoomStatus status) {
        return switch (status) {
            case LIVE -> "This meeting is open and ready to join.";
            case SCHEDULED -> "This meeting has been created but is not open yet.";
            case CLOSED -> "This meeting is already closed.";
        };
    }

    private SpanBadge statusBadge(RoomStatus status) {
        return switch (status) {
            case LIVE -> new SpanBadge("Live", "badge-live");
            case SCHEDULED -> new SpanBadge("Scheduled", "badge-scheduled");
            case CLOSED -> new SpanBadge("Closed", "badge-closed");
        };
    }

    private static final class SpanBadge extends Div {
        private SpanBadge(String label, String className) {
            setText(label);
            addClassName("status-badge");
            addClassName(className);
        }
    }
}
