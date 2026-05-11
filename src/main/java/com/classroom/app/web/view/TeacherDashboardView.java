package com.classroom.app.web.view;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import com.classroom.app.domain.VirtualRoom;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.ClassroomService;
import com.classroom.app.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Route(value = "teacher", layout = MainLayout.class)
@PageTitle("Teacher Dashboard - ClassRoom Live")
@RolesAllowed("TEACHER")
public class TeacherDashboardView extends VerticalLayout {
    public TeacherDashboardView(TeacherRepository teacherRepository, ClassroomService classroomService, RoomService roomService) {
        addClassName("mobile-container");
        addClassName("teacher-dashboard-page");
        addClassName("page-stack");

        Teacher teacher = teacherRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        List<Classroom> classrooms = classroomService.byTeacher(teacher);
        List<VirtualRoom> rooms = roomService.roomsByTeacher(teacher);

        add(buildOverviewBanner(teacher, classrooms.size(), rooms.size()));
        add(buildActionSection());
    }

    private VerticalLayout buildOverviewBanner(Teacher teacher, int classCount, int roomCount) {
        VerticalLayout banner = new VerticalLayout();
        banner.addClassName("hero-panel");
        banner.setPadding(false);
        banner.setSpacing(false);
        banner.add(
                kicker("Teacher dashboard"),
                title("Welcome back, " + teacher.getFullName()),
                copy("Use the teacher workspace to manage classes, create rooms, and go live."),
                stats(classCount, roomCount)
        );
        return banner;
    }

    private VerticalLayout buildActionSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-band");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Workspace"), sectionTitle("Choose a teacher task"), copy("Open a dedicated page for the work you want to do."));

        Div grid = new Div();
        grid.addClassName("feature-grid");
        grid.add(
                actionCard("Manage Classes", "Create and delete classes.", "Open Classes", ClassesView.class),
                actionCard("Manage Rooms", "Create rooms, open or close rooms, and copy invite details.", "Open Rooms", RoomsView.class),
                actionCard("Go Live", "Enter live classroom sessions and access live controls.", "Open Live", TeacherLiveView.class)
        );
        section.add(grid);
        return section;
    }

    private Div actionCard(String title, String text, String buttonText, Class<? extends VerticalLayout> target) {
        Div card = new Div();
        card.addClassName("feature-card");
        H3 heading = new H3(title);
        Paragraph paragraph = copy(text);
        Button button = new Button(buttonText, e -> getUI().ifPresent(ui -> ui.navigate(target)));
        button.addClassName("primary-btn");
        card.add(heading, paragraph, button);
        return card;
    }

    private Div stats(int classCount, int roomCount) {
        Div row = new Div();
        row.addClassName("feature-grid");
        row.add(metric("Classes", String.valueOf(classCount)));
        row.add(metric("Rooms", String.valueOf(roomCount)));
        row.add(metric("Workspace", "Teacher"));
        return row;
    }

    private Div metric(String label, String value) {
        Div card = new Div();
        card.addClassName("metric-card");
        Span metricLabel = new Span(label);
        metricLabel.addClassName("metric-label");
        Span metricValue = new Span(value);
        metricValue.addClassName("metric-value");
        card.add(metricLabel, metricValue);
        return card;
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
