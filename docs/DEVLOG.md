# Week 01

## DONE
- Initial project setup with Java 21, Spring Boot, and PostgreSQL
- Configured Docker Compose (isolated PostgreSQL port to `5433` to prevent local conflicts)
- Created `Materia` and `SessaoEstudo` JPA entities with proper relational mapping
- Implemented core CRUD endpoints for `Materia` (POST, GET all, DELETE)
- Implemented core CRUD endpoints for `SessaoEstudo` (POST, GET all, PUT, DELETE)
- Added support for ongoing study sessions (allowing `null` end date for real-time tracking)
- Decoupled Controllers and Services using DTOs and added custom color attributes for `Materia`
- Implemented global exception handling and custom date validation logic
- Added initial unit tests for core services and exception scenarios using JUnit 5 and Mockito
- Added initial domain modeling files

## Problems

- **Local Port Collision:** Default PostgreSQL port `5432` was already in use by a local instance.
  - *Solved:* Remapped Docker container port to `5433:5432`.
- **Entity & DTO Layer Coupling:** Early controller logic exposed domain entities directly.
  - *Solved:* Refactored layers to use explicit request/response DTOs for cleaner architecture.

# Semana 02

## DONE
- Implemented Daily and Weekly statistics endpoints
- Created more exceptions and improved GlobalExceptionHandler code
- Documented all DTO schemas and error responses
- Tested documentation via Swagger UI
- Flyway Migrations
- Validation of conflicting timezones error handling
- PATCH close study session

## Problems

Incompatibility of Flyway standard dependency + Spring Boot 4

- solved by looking up on google and found new Dependency to be injected


# Semana 03

## DONE
- Implemented periodic statistics endpoint.
- Time period accepts optional end date (uses present time when no end date specified).
- added validation for invalid time periods (end date before start date)
- manual swagger tests passed
- Implemented SessaoEstudoRepostory tests (@DataJpaTest, assertThat)
- Implemented unit tests for all Service Layers (Mockito)
- Implemented all Control layer tests, including global exception handler

## Notes

- Service layer tests were the most complicated
    - use Mockito when to simulate injected dependencies expected method outcomes.
    - Created private static final attributes to represent common and reused test variables
    - Used auxiliary private constructors to avoid code repetition
    - assertEquals, assertTrue, assertNotNull

- Used MockMvc perform to simulate api endpoint request
    - dummy controller class used to test GlobalExceptionHandler
    - .andExpect

- Repository tests require entity manager
    - .persist(object)
    - @DataJpaTest
    - assertThat

## Problems
- tried to translate the few portuguese commit messages (3) into english, it changed all commit dates between the time period of the first Portuguese one to commited today, so I preferred to leave them in portuguese maintaining all correct commit dates.



# Semana 04

## DONE
- Implemented CI with github actions.
- Implemented application-dev profile


## Notes

- CI implemented thorugh creation of .yml in .github/worflows (root of repo)
    - implemented tests whenever push / pull on main branch to github
- Moved most of application.properties to dev profile (datasource, jpa, flyway)
    -  now standar run in IntelliJ runs in dev profile (added environmental variable to run config)

## Problems
- Initial CI config caused a bunch of errors
    - had to add @ActiveProfile("test") to main (MonitorEstudosApplicationTests)test class

# Semana 05
- Added complete readme documentation of the project
- Started front end with angular

# Semana 06

## DONE
- Configured CORS for Angular frontend integration.

## Notes
- Integrated frontend running on `http://localhost:4200` with Spring Boot backend on `http://localhost:8080`.
- Added global CORS configuration via `WebMvcConfigurer` implementation (`WebConfig` class) allowing `http://localhost:4200` origins and standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`).
- Annotated `MateriaController` with `@CrossOrigin(origins = "http://localhost:4200")` for explicit route mapping.

## Problems
- Initial request from Angular returned `status 0 / net::ERR_FAILED` due to CORS blocking.
- Browser triggered an unhandled `NoResourceFoundException` for `/favicon.ico` which was caught by `GlobalExceptionHandler` returning HTTP 500.
    - Resolved endpoint URL alignment from `/materias` to `/monitor-estudos/materias`.
