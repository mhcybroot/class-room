package com.classroom.app.web.view;

import com.classroom.app.config.AppContentProperties;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "contact", layout = MainLayout.class)
@PageTitle("Contact Us - ClassRoom Live")
@AnonymousAllowed
public class ContactView extends VerticalLayout {
    public ContactView(AppContentProperties props) {
        addClassName("mobile-container");
        addClassName("page-stack");

        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("hero-panel");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(kicker("Support and onboarding"), title(props.contactTitle()), copy(props.contactBody()));
        add(hero);

        add(section(
                "Support channels",
                "Reach the classroom support team",
                "Use the support details below when you need onboarding help, classroom setup guidance, or technical assistance.",
                grid("support-grid",
                        supportCard("Support email", props.supportEmail(), "Best for onboarding, teacher access, and general support questions."),
                        supportCard("Support phone", props.supportPhone(), "Best for urgent coordination before a scheduled class or live session.")
                )
        ));

        add(section(
                "What support can help with",
                "Common reasons teams contact us",
                "Support is structured around classroom readiness, teacher workflows, and student access clarity.",
                grid("feature-grid",
                        featureCard("Teacher onboarding", "Get help with login, teacher setup, and initial dashboard access."),
                        featureCard("Classroom configuration", "Confirm class publishing, room details, and classroom invite flow."),
                        featureCard("Student access", "Resolve room code, classroom state, and join-flow confusion before class starts."),
                        featureCard("Live session readiness", "Check that devices, room state, and classroom opening steps are working as expected."),
                        featureCard("Support navigation", "Use clear contact paths and product pages to reach the right workflow faster."),
                        featureCard("General product questions", "Learn how the platform is meant to be used by teachers and students." )
                )
        ));

        add(section(
                "Teacher access",
                "Need to manage classes right now?",
                "Teachers can go directly to login and manage classes, rooms, and invite flows from the dashboard.",
                ctaRow()
        ));
    }

    private VerticalLayout section(String kicker, String title, String copy, Div content) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-band");
        section.setPadding(false);
        section.setSpacing(false);
        section.add(kicker(kicker), sectionTitle(title), copy(copy), content);
        return section;
    }

    private Div grid(String className, Div... items) {
        Div grid = new Div();
        grid.addClassName(className);
        grid.add(items);
        return grid;
    }

    private Div supportCard(String title, String value, String detail) {
        Div card = new Div();
        card.addClassName("support-card");
        card.add(new H3(title), copy(value), copy(detail));
        return card;
    }

    private Div featureCard(String title, String text) {
        Div card = new Div();
        card.addClassName("feature-card");
        card.add(new H3(title), copy(text));
        return card;
    }

    private Div ctaRow() {
        Div row = new Div();
        row.addClassName("footer-links");
        row.add(link("/login", "Teacher login"), link("/about", "About platform"));
        return row;
    }

    private Anchor link(String href, String label) {
        Anchor anchor = new Anchor(href, label);
        anchor.addClassName("footer-link");
        return anchor;
    }

    private Paragraph kicker(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("section-kicker");
        return p;
    }

    private H1 title(String text) {
        H1 h1 = new H1(text);
        h1.addClassName("info-title");
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
}
