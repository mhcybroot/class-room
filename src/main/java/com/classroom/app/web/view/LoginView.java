package com.classroom.app.web.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.core.env.Environment;

@Route("login")
@PageTitle("Teacher Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final LoginForm form;
    private final Environment environment;

    public LoginView(Environment environment) {
        this.environment = environment;
        addClassName("mobile-container");
        addClassName("page-stack");

        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("hero-panel");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(
                kicker("Teacher access"),
                title("Sign in to manage classrooms and live teaching sessions."),
                copy("Teachers can create classes, manage room availability, and support students through a clear virtual classroom workflow.")
        );
        add(hero);

        Div shell = new Div();
        shell.addClassName("login-shell");

        VerticalLayout benefits = new VerticalLayout();
        benefits.addClassName("section-band");
        benefits.setPadding(false);
        benefits.setSpacing(false);
        benefits.add(
                kicker("What teachers can do"),
                sectionTitle("Classroom management in one place"),
                copy("The teacher dashboard is designed to keep class setup, room control, and student access organized in one product flow."),
                features()
        );

        VerticalLayout loginCard = new VerticalLayout();
        loginCard.addClassName("surface-card");
        loginCard.addClassName("login-card");
        loginCard.addClassName("form-rail");
        loginCard.setPadding(false);
        loginCard.setSpacing(false);
        loginCard.add(kicker("Teacher sign in"), sectionTitle("Welcome back"), copy("Use your teacher account to access classroom and room controls."));

        form = new LoginForm();
        form.setAction("login");
        form.setForgotPasswordButtonVisible(false);
        loginCard.add(form);

        if (!isProduction()) {
            Paragraph helper = new Paragraph("Dev hint: teacher@example.com / ChangeMe123!");
            helper.addClassName("helper-text");
            loginCard.add(helper);
        }

        Div footerLinks = new Div();
        footerLinks.addClassName("footer-links");
        footerLinks.add(link("/", "Back to Home"), link("/contact", "Need help? Contact support"));
        loginCard.add(footerLinks);

        shell.add(benefits, loginCard);
        add(shell);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean hasError = event.getLocation().getQueryParameters().getParameters().containsKey("error");
        form.setError(hasError);
    }

    private Div features() {
        Div grid = new Div();
        grid.addClassName("feature-grid");
        grid.add(
                featureCard("Create and organize classes", "Build classroom structure before opening live sessions."),
                featureCard("Control room lifecycle", "Open, close, and manage room availability from one dashboard."),
                featureCard("Support student access", "Share room links and invite details in a more organized way.")
        );
        return grid;
    }

    private Div featureCard(String title, String text) {
        Div card = new Div();
        card.addClassName("feature-card");
        card.add(new H3(title), copy(text));
        return card;
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
        h1.addClassName("login-title");
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

    private boolean isProduction() {
        String value = environment.getProperty("vaadin.productionMode", "false");
        return "true".equalsIgnoreCase(value);
    }
}
