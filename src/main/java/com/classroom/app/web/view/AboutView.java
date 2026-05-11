package com.classroom.app.web.view;

import com.classroom.app.config.AppContentProperties;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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
        addClassName("info-page");
        H1 title = new H1(props.aboutTitle());
        title.addClassName("page-title");

        VerticalLayout card = new VerticalLayout(title, new Paragraph(props.aboutBody()));
        card.addClassName("surface-card");
        card.setPadding(false);
        card.setSpacing(false);
        add(card);
    }
}
