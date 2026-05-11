# ClassRoom Live (Spring Boot + Vaadin + LiveKit)

## Requirements
- Java 21
- Maven 3.9+
- PostgreSQL
- Self-hosted LiveKit server

## Configure
Edit `src/main/resources/application.properties`:
- `spring.datasource.*`
- `livekit.url`
- `livekit.api-key`
- `livekit.api-secret`
- `app.bootstrap-admin.*`
- `app.content.*`

## Run
```bash
mvn spring-boot:run
```

Open:
- Home: `http://localhost:8080/`
- Teacher Login: `http://localhost:8080/login`
- Teacher Dashboard: `http://localhost:8080/teacher`

## Features
- Teacher login with Spring Security
- Teacher can add/delete classes
- Teacher can create/open/close virtual rooms
- Student joins with room link + name + phone + PIN
- Room state handling: not started / live / ended
- LiveKit token API for teacher/student
- Mobile-first responsive Vaadin pages

## API
- `POST /api/livekit/token`
- `POST /api/rooms/{id}/open`
- `POST /api/rooms/{id}/close`
- `POST /api/rooms/{id}/mute-all`

## Tests
```bash
mvn test
```

## Security note
Rotate exposed LiveKit credentials before production use.
