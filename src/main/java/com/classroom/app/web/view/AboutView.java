package com.classroom.app.web.view;

import com.classroom.app.config.AppContentProperties;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "about", layout = MainLayout.class)
@PageTitle("About Us")
@AnonymousAllowed
public class AboutView extends VerticalLayout {
    public AboutView(AppContentProperties props) {
        addClassName("mobile-container");
        addClassName("page-stack");

        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("hero-panel");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(kicker("About the platform"), title(props.aboutTitle()), copy(props.aboutBody()));
        add(hero);

        add(section(
                "Product purpose",
                "Built to make classroom access easier",
                "The platform helps teachers publish classes and helps students join live sessions through a predictable, secure flow.",
                grid("feature-grid",
                        featureCard("For teachers", "Create classes, manage virtual rooms, and guide students into live sessions without extra complexity."),
                        featureCard("For students", "Join from a code or link, confirm details, and enter class after a quick device check."),
                        featureCard("For support teams", "Keep onboarding and troubleshooting clear with predictable room states and contact pathways.")
                )
        ));

        add(section(
                "Why organizations use it",
                "Clear, structured, and dependable",
                "Schools and training teams benefit when classroom access is easy to explain and easy to follow.",
                grid("value-grid",
                        featureCard("Simple class access", "The join flow reduces confusion before class begins."),
                        featureCard("Teacher control", "Teachers can open rooms, manage sessions, and share invite information clearly."),
                        featureCard("Mobile friendly", "The interface stays usable for students and teachers who rely on phones."),
                        featureCard("Clear room states", "Students understand whether a class is live, scheduled, or closed."),
                        featureCard("Support ready", "Onboarding and troubleshooting can be routed through a clear contact page."),
                        featureCard("Consistent experience", "Public pages and teacher workflows follow one consistent product system.")
                )
        ));

        add(section(
                "Next step",
                "Need help getting started?",
                "Visit the contact page for support details or use the teacher login to manage classes and rooms.",
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

    private Div featureCard(String title, String text) {
        Div card = new Div();
        card.addClassName("feature-card");
        card.add(new H3(title), copy(text));
        return card;
    }

    private Div ctaRow() {
        Div row = new Div();
        row.addClassName("footer-links");
        row.add(link("/contact", "Contact support"), link("/login", "Teacher login"));
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
