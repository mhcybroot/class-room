# Vaadin 25 Soul — ClassRoom Live Best Practices

> This file is the authoritative Vaadin best practices guide for the `/opt/support/class-room` project.
> It captures patterns learned from Vaadin 25 docs, real bugs encountered, and correct approaches.
> Read this before writing Vaadin code for this project.

---

## 1. THEME & CSS (CRITICAL — We broke this before)

### The `:host` Bug We Fixed
**Problem:** CSS custom properties in `styles.css` were wrapped in `:host { }`. This scopes variables to Vaadin's shadow DOM root — invisible to `html`, `body`, and all regular DOM elements. The page rendered with Lumo's default white theme.

**Fix:** Use `:root { }` NOT `:host { }` for global CSS variables:
```css
/* WRONG — scoped to shadow DOM */
:host {
  --color-bg: #08090a;
  --color-brand: #5e6ad2;
}

/* CORRECT — on document root */
:root {
  --color-bg: #08090a;
  --color-brand: #5e6ad2;
}
```

### Theme Loading in Vaadin 25
- `@Theme("classroomapp")` on `AppShellConfigurator` loads `themes/classroomapp/` from `src/main/frontend/`
- `theme.json` controls Lumo imports: `{"lumoImports": ["typography", "color", "spacing", "badge", "utility"]}`
- `styles.css` is loaded automatically as the theme's main stylesheet
- CSS variables cascade from `:root` to all descendants, including shadow DOM components
- Lumo `--lumo-*` variables override defaults for ALL components globally

### Lumo Dark Mode
```css
/* In styles.css — override Lumo base colors */
:root {
  --lumo-base-color: #0d0e11;        /* Card/panel backgrounds */
  --lumo-body-text-color: #f5f5f5;   /* Default text */
  --lumo-primary-color: #5e6ad2;    /* Buttons, links, brand */
  --lumo-primary-text-color: #ffffff;
}
```

### Styling Vaadin Components
Use `::part()` pseudo-element for component internals:
```css
vaadin-text-field::part(input-field) {
  background: var(--color-surface-elevated);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
}

vaadin-button::part(prefix), vaadin-button::part(suffix) {
  /* style icons inside button */
}
```

### Scope styles with class names
```java
textField.addClassName("my-custom-field");
// Then in CSS:
vaadin-text-field.my-custom-field::part(input-field) { ... }
```

### Add extra stylesheets
```java
@Route(value = "login", autoLayout = false)
@AnonymousAllowed
public class LoginView extends VerticalLayout {
    // NOT recommended — use theme CSS instead
    // @StyleSheet("https://fonts.googleapis.com/...")
}
```

---

## 2. SECURITY — Critical Bug We Found

### The `@RolesAllowed` Crash
**Problem:** `/teacher` and `/teacher/classes` threw `NoSuchElementException` because:
1. `@RolesAllowed("TEACHER")` on the view triggers access denial
2. Vaadin tries to redirect to the login view
3. BUT the router LAYOUT (`MainLayout`) also gets checked for access
4. `MainLayout` has no security annotation → defaults to `@DenyAll`
5. Access denied happens BEFORE the redirect, throwing an exception

**FIX — Add `@PermitAll` to `MainLayout`:**
```java
@Layout
@PermitAll  // REQUIRED — router layouts must allow access
public class MainLayout extends AppLayout implements AfterNavigationObserver {
    // ...
}
```

### Security Annotations Reference
```java
@AnonymousAllowed   // Anyone including unauthenticated (use on login, home, public pages)
@PermitAll          // Any authenticated user
@RolesAllowed("TEACHER")  // Only TEACHER role
@DenyAll           // Block everyone (default for all routes)
```

### Important Rules
- `@AnonymousAllowed` is Vaadin-specific — prefer it over Spring's `@PermitAll` for public pages
- Spring Security annotations (`@Secured`, `@PreAuthorize`) are NOT checked on views — use Jakarta annotations only
- Annotations on superclasses ARE inherited
- Annotations on interfaces are NOT checked
- Router layouts MUST have `@PermitAll` (or `@AnonymousAllowed`) or all child views fail to load

### Spring Security Config for Vaadin
```java
@EnableWebSecurity
@Configuration
class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), cfg -> {
            cfg.loginView(LoginView.class);  // redirect here on auth failure
        });
        http.csrf().disable();  // Vaadin handles CSRF via UIDL token
        return http.build();
    }
}
```

---

## 3. ROUTING & NAVIGATION

### Correct Route Patterns
```java
@Route(value = "", layout = MainLayout.class)     // Root URL — "value = """ not "/"
@Route(value = "teacher", layout = MainLayout.class)
@Route(value = "teacher/classes", layout = MainLayout.class)
@Route(value = "login", autoLayout = false)       // No parent layout — custom login page
```

### Navigation Observers
```java
// Before navigation — guard access, redirect
public class TeacherDashboardView extends VerticalLayout 
    implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!isAuthorized()) {
            event.redirectToLogin();  // NOT event.rerouteTo(LoginView.class)
        }
    }
}

// After navigation — update UI state (active nav links, etc.)
public class MainLayout extends AppLayout 
    implements AfterNavigationObserver {
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        updateActiveLink(event.getLocation().getPath());
    }
}
```

### Page Title
```java
@Route(value = "", layout = MainLayout.class)
@PageTitle("ClassRoom Live - Virtual Classroom")  // Declarative preferred
```

---

## 4. APP LAYOUT — MainLayout Patterns

### Correct Structure (our current approach is mostly right)
```java
@Layout
@PermitAll  // REQUIRED
public class MainLayout extends AppLayout 
    implements AfterNavigationObserver {
    
    public MainLayout() {
        // 1. Skip link for accessibility
        Anchor skipLink = new Anchor("#main-content", "Skip to main content");
        skipLink.addClassName("skip-link");
        addToNavbar(skipLink);
        
        // 2. Brand
        Div brand = new Div();
        brand.addClassName("site-brand-wrap");
        brand.add(new Span("ClassRoom Live"), new Span("Virtual Classroom Platform"));
        
        // 3. Navigation
        Nav nav = new Nav();
        nav.addClassName("site-nav");
        nav.add(new Anchor("/", "Home"), new Anchor("/login", "Teacher Login"));
        
        // 4. Combine in header
        Header header = new Header();
        header.addClassName("site-header");
        HorizontalLayout headerLayout = new HorizontalLayout(brand, nav, mobileMenuButton);
        headerLayout.addClassName("site-header-inner");
        header.add(headerLayout);
        
        addToNavbar(header);
    }
}
```

### Missing CSS Classes (fix these)
- `site-trust-pill` — referenced in MainLayout but not in `styles.css` → add it
- `site-trust-bar` — referenced in MainLayout but no CSS for it

---

## 5. COMPONENT PATTERNS

### Use Lumo Variants First
Before writing custom CSS, check if Lumo has a variant:
```java
button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
button.addThemeVariants(ButtonVariant.LUMO_ERROR);
button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
```

### Binder for Forms
```java
Binder<Teacher> binder = new Binder<>();
TextField nameField = new TextField("Name");
binder.forField(nameField)
    .withValidator(new StringLengthBehavior("Name required", 2, 100))
    .bind(Teacher::getFullName, Teacher::setFullName);

binder.readBean(teacher);  // populate form
binder.writeBean(teacher);  // save form
```

### Dialog Patterns
```java
Dialog dialog = new Dialog();
dialog.setHeaderTitle("Confirm Delete");
dialog.add(new Text("Are you sure?"));
dialog.getFooter().add(
    new Button("Cancel", e -> dialog.close()),
    new Button("Delete", e -> { /* action */ dialog.close(); })
);
dialog.open();
```

### Notification Variants
```java
Notification.show("Saved", 3000, Notification.Position.TOP_CENTER);
// Or with variant:
Notification notification = new Notification("Error");
notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
notification.open();
```

---

## 6. BUILD & FRONTEND TOOLS

### Vaadin Maven Plugin
```xml
<plugin>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-frontend</goal>   <!-- Generates TypeScript, copies resources -->
                <goal>build-frontend</goal>      <!-- Compiles bundle in production -->
            </goals>
        </execution>
    </executions>
</plugin>
```

### Key Commands
```bash
mvn vaadin:prepare-frontend    # Sets up frontend (run after frontend deps change)
mvn spring-boot:run            # Dev mode with live reload
mvn package -Pproduction        # Production build
```

### JAR Resources Copy Bug (Known Issue)
`vaadin:prepare-frontend` copies resources to `src/main/frontend/generated/jar-resources/`. Spring Boot repackaging moves these to `BOOT-INF/classes`. If Lit components (gridConnector, etc.) 404 in production, add to `pom.xml`:
```xml
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-jar-resources</id>
            <phase>generate-resources</phase>
            <goals><goal>copy-resources</goal></goals>
            <configuration>
                <outputDirectory>${project.build.outputDirectory}/META-INF/resources/frontend</outputDirectory>
                <resources>
                    <resource><directory>src/main/frontend/generated/jar-resources</directory></resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 7. COMMON PITFALLS

### 1. `orElseThrow()` without null check
**BAD:**
```java
Teacher teacher = teacherRepository.findByEmail(email).orElseThrow();
// Throws NoSuchElementException if not found
```
**GOOD:**
```java
Optional<Teacher> teacher = teacherRepository.findByEmail(email);
if (teacher.isEmpty()) {
    getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    return;
}
// or use orElse(null) with null check
```

### 2. Button width 100% inside VerticalLayout
Vaadin buttons have `width: undefined` by default. Use `.setWidthFull()` or `.setWidth("100%")`:
```java
Button button = new Button("Submit");
button.setWidthFull();  // Or add class name "primary-btn" which sets width
```

### 3. setSpacing(false) not inherited
`setSpacing(false)` only affects direct children. Use `setSpacing(false)` on the layout itself AND ensure children don't get spacing applied from parent.

### 4. Vaadin Dev Tools overlay on top of content
The Vaadin development mode indicator (`! Development Mode`) can cover content. In production it disappears. This is expected.

### 5. Camera/Microphone blocked on HTTP
Browsers require HTTPS for `navigator.mediaDevices`. The app runs on `http://localhost:8091` for dev — camera works locally. For public access use HTTPS.

---

## 8. PROJECT STRUCTURE

```
src/main/java/com/classroom/app/
├── ClassRoomApplication.java          # Main — no annotations needed here
├── config/
│   ├── VaadinThemeConfig.java         # @Theme("classroomapp"), @PWA, AppShellConfigurator
│   ├── SecurityConfig.java            # Spring Security + Vaadin
│   ├── LiveKitProperties.java        # @ConfigurationProperties
│   └── AppContentProperties.java      # @ConfigurationProperties
├── domain/                            # JPA entities
│   ├── Classroom.java
│   ├── VirtualRoom.java
│   ├── Teacher.java
│   └── SessionParticipant.java
├── repo/                              # Spring Data JPA
├── service/                           # Business logic
├── web/
│   ├── AppExceptionHandler.java        # @ControllerAdvice
│   ├── LiveKitController.java         # REST API
│   ├── RoomController.java            # REST API
│   └── view/
│       ├── MainLayout.java            # @Layout @PermitAll — MUST have @PermitAll
│       ├── HomeView.java              # @Route(""), @AnonymousAllowed
│       ├── LoginView.java             # @Route("login", autoLayout=false), @AnonymousAllowed
│       ├── TeacherDashboardView.java   # @Route("teacher"), @RolesAllowed("TEACHER")
│       ├── ClassesView.java           # @Route("teacher/classes"), @RolesAllowed("TEACHER")
│       ├── RoomsView.java             # @Route("teacher/rooms"), @RolesAllowed("TEACHER")
│       └── StudentJoinView.java        # @Route("join/{roomCode}"), @AnonymousAllowed
src/main/frontend/
├── themes/classroomapp/
│   ├── theme.json                     # lumoImports
│   └── styles.css                     # Global CSS (USE :root NOT :host)
└── components/                         # Custom CSS components
src/main/resources/
├── application.properties
├── db/migration/V1__init.sql          # Flyway migrations
└── META-INF/VAADIN/                   # Vaadin internal resources
```

---

## 9. SPRING SECURITY + VAADIN — Current State

Our current `SecurityConfig` uses `VaadinSpringSecurityService` integration. Key patterns:

### Super.configure(http) Order Matters
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);  // MUST be first — sets up Vaadin CSRF, request cache
    http.authorizeRequests()
        .requestMatchers("/login", "/logout").permitAll()
        .requestMatchers("/api/**").permitAll()  // REST APIs
        .anyRequestauthenticated();  // Everything else needs auth
}
```

### CSRF — Vaadin handles it
Do NOT disable CSRF with `.csrf().disable()` globally. Vaadin uses its own CSRF token via UIDL. The exception is for non-Vaadin endpoints (REST APIs) — handle those separately.

---

*Last updated: 2026-05-12 — Based on Vaadin 25.1.5 docs + real bugs found in class-room app*
