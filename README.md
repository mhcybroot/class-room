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

## HTTPS for Camera/Microphone in Production
Browsers block `navigator.mediaDevices` on insecure origins.  
If you open this app as `http://<public-ip>:8091`, camera/microphone will not work.

Use HTTPS in production:

1. Create/load a PKCS12 keystore (`.p12`) with your TLS certificate.
2. Run with the `prod` profile and SSL env vars:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SSL_KEY_STORE=/path/to/keystore.p12
export SSL_KEY_STORE_PASSWORD=changeit
export SSL_KEY_ALIAS=tomcat
mvn spring-boot:run
```

Then open: `https://your-domain-or-ip:8091`

Notes:
- `localhost` works for local testing without HTTPS, but public IP/domain requires HTTPS.
- For LiveKit media transport in production, set `livekit.url` to your secure endpoint (`wss://...`) where appropriate.
