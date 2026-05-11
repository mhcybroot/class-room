package com.classroom.app.web.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
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
    private final Nav nav = new Nav();
    private final Button mobileMenuButton = new Button("⋯");

    public MainLayout() {
        Div brandWrap = new Div();
        brandWrap.addClassName("site-brand-wrap");

        Span brandTitle = new Span("ClassRoom Live");
        brandTitle.addClassName("site-brand-title");
        Span brandSubtitle = new Span("Virtual Classroom Platform");
        brandSubtitle.addClassName("site-brand-subtitle");
        brandWrap.add(brandTitle, brandSubtitle);

        nav.addClassName("site-nav");
        mobileMenuButton.addClassName("mobile-menu-button");
        mobileMenuButton.getElement().setAttribute("aria-label", "Open navigation menu");
        mobileMenuButton.getElement().setAttribute("title", "Open navigation menu");
        mobileMenuButton.addClickListener(event -> {
            var classList = nav.getElement().getClassList();
            if (classList.contains("nav-open")) {
                classList.remove("nav-open");
            } else {
                classList.add("nav-open");
            }
        });

        if (isTeacherAuthenticated()) {
            addTeacherLinks();
        } else {
            addPublicLinks();
        }
        navLinks.values().forEach(nav::add);

        Header topHeader = new Header();
        topHeader.addClassName("site-header");

        HorizontalLayout header = new HorizontalLayout(brandWrap, nav, mobileMenuButton);
        header.addClassName("site-header-inner");
        header.setWidthFull();
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        topHeader.add(header);

        Div trustBar = new Div();
        trustBar.addClassName("site-trust-bar");
        Div trustInner = new Div();
        trustInner.addClassName("site-trust-inner");
        Span trustCopy = new Span(isTeacherAuthenticated()
                ? "Teacher workspace"
                : "Join classes with room code or teacher invite");
        trustCopy.addClassName("site-trust-copy");
        trustInner.add(trustCopy, trustPill(isTeacherAuthenticated() ? "Teacher mode" : "Student access"));
        trustBar.add(trustInner);

        addToNavbar(topHeader, trustBar);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String currentPath = "/" + event.getLocation().getPath();
        if ("/".equals(currentPath)) {
            currentPath = "/";
        }
        for (Map.Entry<String, Anchor> entry : navLinks.entrySet()) {
            if (currentPath.equals(entry.getKey()) || ("/teacher".equals(entry.getKey()) && currentPath.startsWith("/teacher"))) {
                entry.getValue().addClassName("active-link");
            } else {
                entry.getValue().removeClassName("active-link");
            }
        }
        nav.getElement().getClassList().remove("nav-open");
    }

    private void addPublicLinks() {
        navLinks.put("/", navAnchor("/", "Home", false));
        navLinks.put("/login", navAnchor("/login", "Teacher Login", true));
    }

    private void addTeacherLinks() {
        navLinks.put("/teacher", navAnchor("/teacher", "Dashboard", false));
        navLinks.put("/teacher/classes", navAnchor("/teacher/classes", "Classes", false));
        navLinks.put("/teacher/rooms", navAnchor("/teacher/rooms", "Rooms", false));
        navLinks.put("/teacher/live", navAnchor("/teacher/live", "Live", false));
        navLinks.put("/logout", navAnchor("/logout", "Logout", true));
    }

    private Anchor navAnchor(String href, String label, boolean authAction) {
        Anchor anchor = new Anchor(href, label);
        anchor.addClassName("nav-link");
        if (authAction) {
            anchor.addClassName("auth-link");
        }
        return anchor;
    }

    private Span trustPill(String text) {
        Span pill = new Span(text);
        pill.addClassName("site-trust-pill");
        return pill;
    }

    private boolean isTeacherAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}
