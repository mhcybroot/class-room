package com.classroom.app.web.view;

import com.classroom.app.domain.Classroom;
import com.classroom.app.domain.Teacher;
import com.classroom.app.repo.TeacherRepository;
import com.classroom.app.service.ClassroomService;
import com.vaadin.flow.component.button.Button;
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
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Route(value = "teacher/classes", layout = MainLayout.class)
@PageTitle("Manage Classes - ClassRoom Live")
@RolesAllowed("TEACHER")
public class ClassesView extends VerticalLayout {
    public ClassesView(TeacherRepository teacherRepository, ClassroomService classroomService) {
        addClassName("mobile-container");
        addClassName("page-stack");

        Teacher teacher = teacherRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
        add(buildCreateCard(teacher, classroomService));
        add(buildClassesList(teacher, classroomService));
    }

    private VerticalLayout buildCreateCard(Teacher teacher, ClassroomService classroomService) {
        TextField className = new TextField("Class Name");
        TextField subject = new TextField("Subject");
        Button create = new Button("Create Class", e -> {
            try {
                classroomService.create(teacher, className.getValue(), subject.getValue());
                notifySuccess("Class created");
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (RuntimeException ex) {
                notifyError(ex.getMessage());
            }
        });
        create.addClassName("primary-btn");

        FormLayout form = new FormLayout(className, subject, create);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout card = new VerticalLayout();
        card.addClassName("section-band");
        card.setPadding(false);
        card.setSpacing(false);
        card.add(kicker("Classes"), title("Create class"), copy("Add a class before creating a room."), form);
        return card;
    }

    private VerticalLayout buildClassesList(Teacher teacher, ClassroomService classroomService) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-band");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker("Class list"), title("Your classes"), copy("View and delete classes from here."));

        List<Classroom> classrooms = classroomService.byTeacher(teacher);
        if (classrooms.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.addClassName("surface-card");
            empty.setPadding(false);
            empty.setSpacing(false);
            empty.add(new H3("No classes yet"), copy("Create your first class using the form above."));
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
            card.add(new H3(classroom.getName()), copy("Subject: " + classroom.getSubject()));

            Button delete = new Button("Delete Class", e -> {
                classroomService.deleteForTeacher(classroom.getId(), teacher);
                notifySuccess("Class deleted");
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            delete.addClassName("danger-btn");
            card.add(delete);
            grid.add(card);
        }
        section.add(grid);
        return section;
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
