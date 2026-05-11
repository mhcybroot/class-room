package com.classroom.app.web.view;

import com.classroom.app.domain.RoomStatus;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.VirtualRoomRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed
public class HomeView extends VerticalLayout {
    public HomeView(VirtualRoomRepository roomRepository) {
        addClassName("mobile-container");
        addClassName("home-dashboard");

        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("surface-card");
        hero.addClassName("hero-block");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(new H1("Join Your Class"));
        hero.add(new Paragraph("Mobile-first classroom dashboard for students. Enter a room code or pick a live class."));

        VerticalLayout quickJoin = new VerticalLayout();
        quickJoin.addClassName("surface-card");
        quickJoin.setPadding(false);
        quickJoin.setSpacing(false);
        quickJoin.add(new H3("Quick Join"));
        quickJoin.add(new Paragraph("Paste the room code your teacher shared, then continue."));
        TextField roomCodeInput = new TextField("Room Code");
        roomCodeInput.setPlaceholder("Example: ab12cd34");
        roomCodeInput.setWidthFull();
        Button goJoin = new Button("Go to Join Page", e -> {
            String code = roomCodeInput.getValue() == null ? "" : roomCodeInput.getValue().trim();
            if (!code.isBlank()) {
                getUI().ifPresent(ui -> ui.navigate("join/" + code));
            }
        });
        goJoin.addClassName("primary-btn");
        quickJoin.add(roomCodeInput, goJoin);

        add(hero, quickJoin);

        List<VirtualRoom> rooms = roomRepository.findAll();
        Div section = new Div();
        section.addClassName("room-grid");

        if (rooms.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("surface-card");
            empty.addClassName("empty-state");
            empty.add(new H3("No classes available right now"));
            empty.add(new Paragraph("Teachers will publish classes here. Check again soon."));
            add(empty);
            return;
        }

        for (VirtualRoom room : rooms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("surface-card");
            card.addClassName("room-card");
            card.setPadding(false);
            card.setSpacing(false);

            H3 className = new H3(room.getClassroom().getName());
            className.addClassName("room-title");

            Paragraph subject = new Paragraph("Subject: " + room.getClassroom().getSubject());
            subject.addClassName("meta-line");
            Paragraph teacher = new Paragraph("Teacher: " + room.getTeacher().getFullName());
            teacher.addClassName("meta-line");

            SpanBadge badge = statusBadge(room.getStatus());

            Button join = new Button("Join Class", e -> getUI().ifPresent(ui -> ui.navigate("join/" + room.getRoomCode())));
            join.setEnabled(room.getStatus() == RoomStatus.LIVE);
            join.addClassName("primary-btn");

            HorizontalLayout top = new HorizontalLayout(className, badge);
            top.setWidthFull();
            top.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
            top.addClassName("room-card-head");

            card.add(top, subject, teacher, new Paragraph("Room Code: " + room.getRoomCode()), join);
            section.add(card);
        }

        add(section);
    }

    private SpanBadge statusBadge(RoomStatus status) {
        return switch (status) {
            case LIVE -> new SpanBadge("Live now", "badge-live");
            case SCHEDULED -> new SpanBadge("Class not started", "badge-scheduled");
            case CLOSED -> new SpanBadge("Class ended", "badge-closed");
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
