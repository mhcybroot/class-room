package com.classroom.app.web.view;

import com.classroom.app.config.AppContentProperties;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "contact", layout = MainLayout.class)
@PageTitle("Contact Us")
@AnonymousAllowed
public class ContactView extends VerticalLayout {
    public ContactView(AppContentProperties props) {
        addClassName("mobile-container");
        addClassName("info-page");
        H1 title = new H1(props.contactTitle());
        title.addClassName("page-title");

        VerticalLayout card = new VerticalLayout(
                title,
                new Paragraph(props.contactBody()),
                new Paragraph("Email: " + props.supportEmail()),
                new Paragraph("Phone: " + props.supportPhone())
        );
        card.addClassName("surface-card");
        card.setPadding(false);
        card.setSpacing(false);
        add(card);
    }
}
