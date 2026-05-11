package com.classroom.app.web.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
        addClassName("login-page");

        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("surface-card");
        hero.addClassName("login-hero");
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.add(new H1("Teacher Login"));
        hero.add(new Paragraph("Manage classes, run live sessions, and control your virtual classroom from one dashboard."));

        VerticalLayout loginCard = new VerticalLayout();
        loginCard.addClassName("surface-card");
        loginCard.addClassName("login-card");
        loginCard.setPadding(false);
        loginCard.setSpacing(false);
        loginCard.add(new H3("Welcome back"));

        form = new LoginForm();
        form.setAction("login");
        form.setForgotPasswordButtonVisible(false);
        loginCard.add(form);

        if (!isProduction()) {
            Paragraph helper = new Paragraph("Dev hint: teacher@example.com / ChangeMe123!");
            helper.addClassName("helper-text");
            loginCard.add(helper);
        }

        VerticalLayout footer = new VerticalLayout();
        footer.setPadding(false);
        footer.setSpacing(false);
        footer.addClassName("login-footer");
        footer.add(new Anchor("/", "Back to Home"));
        footer.add(new Anchor("/contact", "Need help? Contact support"));

        add(hero, loginCard, footer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean hasError = event.getLocation().getQueryParameters().getParameters().containsKey("error");
        form.setError(hasError);
    }

    private boolean isProduction() {
        String value = environment.getProperty("vaadin.productionMode", "false");
        return "true".equalsIgnoreCase(value);
    }
}
