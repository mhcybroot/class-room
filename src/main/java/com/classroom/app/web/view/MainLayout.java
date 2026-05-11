package com.classroom.app.web.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;

@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {
    private final Map<String, Anchor> navLinks = new LinkedHashMap<>();

    public MainLayout() {
        H2 brand = new H2("ClassRoom Live");
        brand.addClassName("site-brand");

        Nav nav = new Nav();
        nav.addClassName("site-nav");

        addPublicLinks();
        if (isTeacherAuthenticated()) {
            navLinks.remove("/login");
            navLinks.put("/teacher", navAnchor("/teacher", "Teacher Dashboard"));
            navLinks.put("/logout", navAnchor("/logout", "Logout"));
        }
        navLinks.values().forEach(nav::add);

        Header topHeader = new Header();
        topHeader.addClassName("site-header");
        HorizontalLayout header = new HorizontalLayout(brand, nav);
        header.addClassName("site-header-inner");
        header.setWidthFull();
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        topHeader.add(header);

        addToNavbar(topHeader);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String currentPath = "/" + event.getLocation().getPath();
        if ("/".equals(currentPath)) {
            currentPath = "/";
        }
        for (Map.Entry<String, Anchor> entry : navLinks.entrySet()) {
            if (entry.getKey().equals(currentPath)) {
                entry.getValue().addClassName("active-link");
            } else {
                entry.getValue().removeClassName("active-link");
            }
        }
    }

    private void addPublicLinks() {
        navLinks.put("/", navAnchor("/", "Home"));
        navLinks.put("/about", navAnchor("/about", "About"));
        navLinks.put("/contact", navAnchor("/contact", "Contact"));
        navLinks.put("/login", navAnchor("/login", "Teacher Login"));
    }

    private Anchor navAnchor(String href, String label) {
        Anchor anchor = new Anchor(href, label);
        anchor.addClassName("nav-link");
        return anchor;
    }

    private boolean isTeacherAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}
