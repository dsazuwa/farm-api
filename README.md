# Getting Started

## Prerequisites

- Java 21
- Docker Desktop with WSL integration enabled
- IntelliJ IDEA

## Local Setup

1. **Open the project**

   Clone the repository and open the folder containing `pom.xml`
   in IntelliJ IDEA. Allow Maven to import the dependencies.

2. **Configure the database password**

   Copy `.env.example` to `.env`:

   ```bash
   cp .env.example .env
   ```

   Set `POSTGRES_PASSWORD` in `.env`.

3. **Start PostgreSQL**

   Start Docker Desktop, then run from the project root:

   ```bash
   docker compose up -d
   ```

   Check that the database is healthy:

   ```bash
   docker compose ps
   ```

4. **Configure IntelliJ**

   Open **Run → Edit Configurations** and select the Spring Boot
   application configuration.

   Find **Environment variables**, under **Modify options** if hidden.
   Add `POSTGRES_PASSWORD` with the same value used in `.env`.

   The application reads it through:

   ```properties
   spring.datasource.password=${POSTGRES_PASSWORD}
   ```

   Docker Compose reads `.env` automatically. IntelliJ must also
   provide the variable to the application.

5. **Run the application**

   Apply the run configuration and run the main application class
   using IntelliJ’s Run button.

6. **Verify the application**

   Confirm that the application starts successfully and connects
   to PostgreSQL.

## Stop the Database

```bash
docker compose stop
```

## Reference Documentation

- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
- [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)

## Maven Parent Overrides

Maven inherits elements from the parent POM, including `<license>`
and `<developers>`. This project contains empty overrides to prevent
inheriting those values.

If you switch parent POMs and want to inherit these elements, remove
the corresponding empty overrides.