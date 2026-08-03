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

## Next Week
- CI/CD configuration with github actions
- maybe dev , prod , test profiles
