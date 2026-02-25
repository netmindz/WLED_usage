# GitHub Copilot Instructions for WLED Usage

## Project Overview

WLED Usage is a Spring Boot application written in Kotlin that tracks usage statistics for WLED (an ESP8266/ESP32 LED control firmware). The application collects upgrade events, device information, and provides statistics about WLED installations worldwide.

## Technology Stack

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.4.0
- **Java Version**: 21
- **Build Tool**: Maven
- **Database**: MySQL with Flyway migrations
- **Testing**: JUnit 5, MockK/Mockito-Kotlin, Spring MockMvc
- **Key Libraries**:
  - Spring Data JPA
  - Spring WebSocket
  - SpringDoc OpenAPI (API documentation)
  - Lombok
  - Flyway (database migrations)
  - Micrometer/Prometheus (metrics)

## Project Structure

```
src/main/kotlin/com/github/wled/usage/
├── WledUsageApplication.kt          # Main application entry point
├── config/                          # Configuration classes (CORS, OpenAPI)
├── controller/                      # REST API controllers
├── dto/                             # Data Transfer Objects
├── entity/                          # JPA entities
├── service/                         # Business logic services
├── UDPServer.kt                     # UDP server for data collection
└── repository/                      # Spring Data repositories

src/main/resources/
├── db/migration/                    # Flyway database migrations
├── static/                          # Static web resources
└── application*.yaml                # Application configuration files
```

## Build and Test Commands

- **Build**: `./mvnw clean package`
- **Build without tests**: `./mvnw clean package -DskipTests`
- **Compile only**: `./mvnw clean compile`
- **Run tests**: `./mvnw test`
- **Run application locally**: `./mvnw spring-boot:run`

## Code Style Guidelines

### Kotlin Code Style

1. **Follow Kotlin conventions**: Use idiomatic Kotlin patterns
2. **Naming**:
   - Classes: PascalCase (e.g., `UsageController`)
   - Functions/properties: camelCase (e.g., `recordUpgradeEvent`)
   - Constants: UPPER_SNAKE_CASE
3. **Null Safety**: Leverage Kotlin's null safety features; use `?` for nullable types
4. **Data Classes**: Use data classes for DTOs and simple entities
5. **Constructor Injection**: Prefer primary constructor injection for dependencies

### Spring Boot Patterns

1. **Controllers**: Use `@RestController` with `@RequestMapping` for base path
2. **Services**: Mark service classes with `@Service` annotation
3. **Dependency Injection**: Use constructor injection (primary constructor in Kotlin)
4. **Request/Response**: Use appropriate HTTP methods and status codes
5. **Testing**: Write unit tests using `@WebMvcTest` for controllers and mock dependencies

### Testing Guidelines

1. **Test Naming**: Use backtick strings for descriptive test names (e.g., `` `should extract X-Country-Code header` ``)
2. **Mocking**: Use MockK or Mockito-Kotlin for mocking
3. **Assertions**: Use AssertJ for fluent assertions
4. **Coverage**: Write tests for controllers, services, and critical business logic
5. **Test Structure**: Follow Arrange-Act-Assert pattern

## Database Guidelines

1. **Migrations**: Always use Flyway migrations for schema changes
2. **Migration Naming**: Use format `V{timestamp}__{description}.sql` (e.g., `V2025111701__bootloaderSHA256.sql`)
3. **Entities**: Use JPA annotations for entity classes
4. **Repositories**: Extend Spring Data JPA repository interfaces

## API Guidelines

1. **Base Path**: All APIs should be under `/api` prefix
2. **OpenAPI**: Document APIs using SpringDoc annotations when needed
3. **Headers**: Support custom headers like `X-Country-Code` for additional metadata
4. **Request/Response**: Use DTOs for request and response bodies
5. **Error Handling**: Provide meaningful error responses

## Docker and Deployment

1. **Local Development**: Use `docker-compose-local.yaml`
2. **Production**: Deployment uses `docker-compose-prod.yaml`
3. **Build**: Application is packaged as a Docker image
4. **Configuration**: Environment-specific configs in `application-{profile}.yaml`

## Frontend Dashboard

The dashboard is a **single static HTML file** at `src/main/resources/static/index.html` served by Spring Boot.

- **Charting library**: D3.js v7, loaded from CDN at `https://d3js.org/d3.v7.min.js` (this URL is in the `<script>` tag and works in the browser at runtime)
- **Chart type**: All charts are SVG-based D3.js visualisations rendered directly into the page
- **Adding charts**: Append a new `<div class="chart-section">` block and a corresponding D3.js `<script>` block to `index.html`; the script fetches data from the relevant `/api/stats/…` endpoint and renders an SVG chart
- **No separate build step**: There is no frontend build system (no npm, no webpack); all chart code is inline JavaScript in `index.html`

> **Sandbox note**: The `d3js.org` domain is **blocked by the agent firewall** at build/test time. This is not a problem because the CDN URL is only used by the end user's browser at runtime, not during the build. Do **not** try to `curl`/download `d3.js` locally.

## Data Model

### `Device` entity (table: `device`, PK: `id` String — MAC-derived device ID)

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | Primary key |
| `version` | `String` | Current WLED firmware version |
| `releaseName` | `String` | Human-readable release name |
| `chip` | `String` | MCU type (e.g. `ESP32`, `ESP8266`, `ESP32-S3`) |
| `ledCount` | `Int?` | Number of LEDs configured |
| `isMatrix` | `Boolean?` | Whether device is configured as a matrix |
| `bootloaderSHA256` | `String` | SHA-256 of the bootloader |
| `brand` / `product` | `String?` | Hardware brand/product |
| `flashSize` | `String?` | Flash size (e.g. `4MB`) |
| `partitionSizes` | `String?` | Partition table info |
| `psramSize` | `String?` | PSRAM size (e.g. `2MB`) |
| `psramPresent` | `Boolean?` | Whether PSRAM is present |
| `countryCode` | `String?` | Two-letter ISO country code |
| `repo` | `String?` | Fork/repo identifier |
| `created` | `LocalDateTime?` | First seen (auto-set by Hibernate) |
| `lastUpdate` | `LocalDateTime?` | Last updated (auto-set by Hibernate) |

### `UpgradeEvent` entity (table: `upgrade_event`, PK: auto-generated `Long`)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long?` | Auto-generated PK |
| `device` | `Device` | `@ManyToOne` FK to `device` table |
| `oldVersion` | `String` | Version before upgrade |
| `newVersion` | `String` | Version after upgrade |
| `created` | `LocalDateTime?` | Timestamp (auto-set by Hibernate) |

An `UpgradeEvent` row is recorded every time a device reports a version change. A **new installation** is a `Device` record where `created == lastUpdate` (no prior version seen), while an **upgrade** is an `UpgradeEvent` for an already-known device.

## Repository Query Pattern

Repository methods that aggregate data use Spring Data JPA `@Query` annotations and return `List<Map<String, Any>>`. The service layer maps these to typed DTOs. JPQL projections use alias names as Map keys; native SQL queries (`nativeQuery = true`) are used for date/time grouping that requires database-specific functions (e.g. `DATE_SUB … WEEKDAY`).

```kotlin
// Typical JPQL aggregation
@Query("SELECT d.chip as chip, COUNT(d) as deviceCount FROM Device d GROUP BY d.chip")
fun countDevicesByChip(): List<Map<String, Any>>

// Typical native SQL for date math (MySQL)
@Query(value = "SELECT DATE(DATE_SUB(created, INTERVAL WEEKDAY(created) DAY)) as weekStart, COUNT(*) as deviceCount FROM device WHERE created >= :since GROUP BY weekStart", nativeQuery = true)
fun countNewDevicesByWeek(since: LocalDateTime): List<Map<String, Any>>
```

## Existing API Endpoints

### `POST /api/usage/upgrade`
Records an upgrade/installation event. Body: `UpgradeEventRequest`. Optional header: `X-Country-Code`.

### `GET /api/stats/…`
All stats endpoints are in `StatsController` and return JSON arrays of DTO objects.

| Endpoint | DTO | Description |
|---|---|---|
| `/api/stats/country` | `CountryStats` | Device count by country code |
| `/api/stats/version` | `VersionStats` | Device count by firmware version |
| `/api/stats/chip` | `ChipStats` | Device count by MCU chip type |
| `/api/stats/matrix` | `MatrixStats` | Device count by matrix/non-matrix |
| `/api/stats/flash-size` | `FlashSizeStats` | Device count by flash size |
| `/api/stats/psram-size` | `PsramSizeStats` | Device count by PSRAM size |
| `/api/stats/release-name` | `ReleaseNameStats` | Device count by release name |
| `/api/stats/led-count` | `LedCountRangeStats` | Device count by LED count range |
| `/api/stats/upgrade-vs-installation` | `UpgradeVsInstallationWeeklyStats` | Weekly upgrades vs new installations |

## Special Considerations

1. **UDP Server**: The project includes a UDP server component (`UDPServer.kt`) currently commented out in main
2. **Metrics**: Prometheus metrics are exposed via Spring Actuator
3. **CORS**: Configured for cross-origin requests
4. **Security**: Database passwords should be managed via secrets, not committed to code

## Development Workflow

1. **Branching**: Create feature branches for new work
2. **Testing**: Ensure all tests pass before committing
3. **Build**: Verify the build succeeds with `./mvnw clean package`
4. **Code Quality**: Address any compiler warnings when appropriate
5. **Documentation**: Update OpenAPI specs for API changes

## Common Tasks

### Adding a New REST Endpoint

1. Create/update DTO in `dto/` package
2. Add method to service in `service/` package
3. Create controller method in `controller/` package
4. Write unit tests in `src/test/kotlin/`
5. Update OpenAPI documentation if needed

### Adding a New Dashboard Chart/Widget

1. Create a DTO data class in `dto/` (e.g. `data class MyStats(val label: String, val count: Long)`)
2. Add an aggregation query to the relevant repository in `repository/` returning `List<Map<String, Any>>`
3. Add a service method in `service/StatsService.kt` that calls the repository and maps to the DTO
4. Add a `@GetMapping` in `controller/StatsController.kt` returning the DTO list
5. Add a new `<div class="chart-section">` block and a D3.js `<script>` block in `src/main/resources/static/index.html` that fetches from the new endpoint and renders an SVG chart
6. Write controller and service unit tests

### Adding Database Changes

1. Create new Flyway migration in `src/main/resources/db/migration/`
2. Use proper naming: `V{YYYYMMDDnn}__{description}.sql`
3. Update entity classes if needed
4. Test migration locally

### Adding New Dependencies

1. Add dependency to `pom.xml` in appropriate scope
2. Run `./mvnw clean compile` to verify
3. Update this document if it's a significant addition

## Tips for AI Assistants

- This is a backend service with REST APIs and database persistence
- Kotlin is the primary language; leverage its features
- Follow Spring Boot best practices for dependency injection and component structure
- Always write tests for new functionality
- Database changes require Flyway migrations
- The build process skips tests in CI (noted by TODO in workflow), but tests should still be written
