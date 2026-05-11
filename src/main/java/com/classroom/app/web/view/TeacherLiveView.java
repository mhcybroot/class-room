package com.classroom.app.web.view;

import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@Route(value = "teacher/live", layout = MainLayout.class)
@PageTitle("Live")
@RolesAllowed("TEACHER")
public class TeacherLiveView extends VerticalLayout {
    public TeacherLiveView(TeacherRepository teacherRepository, RoomService roomService) {
        addClassName("mobile-container");
        addClassName("page-stack");

        Teacher teacher = teacherRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        add(buildHeader());
        add(buildLiveRoomsSection(roomService.roomsByTeacher(teacher)));
    }

    private VerticalLayout buildHeader() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("hero-panel");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Live"), title("Go live"), copy("Enter a live room from here after it has been opened on the Rooms page."));
        return section;
    }

    private VerticalLayout buildLiveRoomsSection(List<VirtualRoom> rooms) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-band");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Live rooms"), sectionTitle("Teacher live access"), copy("Open rooms from the Rooms page, then enter the live classroom from here."));

        if (rooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.addClassName("surface-card");
            empty.setPadding(false);
            empty.setSpacing(false);
            empty.add(new H3("No rooms available"), copy("Create a room first from the Rooms page."));
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

            HorizontalLayout top = new HorizontalLayout(new H3(room.getClassroom().getName()), statusBadge(room.getStatus().name()));
            top.setWidthFull();
            top.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
            top.addClassName("room-card-head");

            Div meta = new Div();
            meta.addClassName("meta-chip-row");
            meta.add(metaChip("Room", room.getRoomCode()));
            meta.add(metaChip("Subject", room.getClassroom().getSubject()));

            Button enter = new Button("Enter Live Room", e -> getUI().ifPresent(ui -> ui.navigate(
                    LiveRoomView.class,
                    new RouteParameters(Map.of("role", "teacher", "roomId", room.getId().toString()))
            )));
            enter.setEnabled("LIVE".equalsIgnoreCase(room.getStatus().name()));
            enter.addClassName("primary-btn");

            card.add(top, meta, copy("Only live rooms can be entered from this page."), enter);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private Div metaChip(String label, String value) {
        Div chip = new Div();
        chip.addClassName("meta-chip");
        chip.setText(label + ": " + value);
        return chip;
    }

    private Span statusBadge(String text) {
        Span badge = new Span(text);
        badge.addClassName("status-badge");
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
}
