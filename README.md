# TaskFlow - Full-Stack Task Management API

![Java](https://img.shields.io/badge/Java-21-red)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30-purple)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-✓-2496ED)
![GitHub Actions](https://github.com/mabotjatr/taskflow-backend/workflows/TaskFlow%20CI/CD%20Pipeline/badge.svg)
![Swagger](https://img.shields.io/badge/API-Documented-green)

A production-ready task management API with JWT authentication, PostgreSQL, Docker, and complete CI/CD pipeline.

## 🚀 Quick Start

### Using Docker Compose (Recommended)
```bash
# Clone the repository
git clone https://github.com/mabotjatr/taskflow-backend.git
cd taskflow-backend

# Start the application
docker-compose up --build

# API will be available at: http://localhost:8081

# taskflow-backend

This project uses Quarkus, the Supersonic Subatomic Java Framework.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## API Documentation

### Interactive API Explorer
The API is fully documented with OpenAPI/Swagger. You can explore and test endpoints interactively:

- **Swagger UI**: [http://localhost:8081/swagger-ui](http://localhost:8081/swagger-ui)
### Available Endpoints

#### Authentication
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/auth/register` | Register new user | None           |
| `POST` | `/auth/login` | Login user | None           |

#### Task Management
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|--------------|
| `GET` | `/tasks` | Get all tasks for current user | JWT Required |
| `GET` | `/tasks/{id}` | Get specific task by ID | JWT Required |
| `POST` | `/tasks` | Create new task | JWT Required |
| `PUT` | `/tasks/{id}` | Update existing task | JWT Required |
| `DELETE` | `/tasks/{id}` | Delete task | JWT Required |


## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/taskflow-backend-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- SmallRye JWT Build ([guide](https://quarkus.io/guides/security-jwt-build)): Create JSON Web Token with SmallRye JWT Build API
- SmallRye JWT ([guide](https://quarkus.io/guides/security-jwt)): Secure your applications with JSON Web Token
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
# Test CI/CD
