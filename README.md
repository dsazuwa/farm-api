# Getting Started

## Prerequisites

- Java 21
- Docker Desktop with WSL integration enabled
- IntelliJ IDEA

## Local Setup

### 1. Open the project

Clone the repository and open the folder containing `pom.xml` in
IntelliJ IDEA. Allow Maven to import the dependencies.

Set the **Project SDK** to Java 21 under **File → Project Structure → Project**.

### 2. Configure environment variables

Copy the example environment file:

```bash
cp .env.example .env
```

React uses Supabase Auth for login. Spring Boot validates the access
tokens, while application data remains in local Docker PostgreSQL.

Use the same Supabase project as the React app.

The backend requires:

```dotenv
POSTGRES_PASSWORD=your_local_database_password
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co

SEED_PATRICK_AUTH_ID=actual_supabase_user_uuid
SEED_NELSON_AUTH_ID=actual_supabase_user_uuid
SEED_DANIELLE_AUTH_ID=actual_supabase_user_uuid
SEED_OKPARA_AUTH_ID=actual_supabase_user_uuid
```

For development seeding, create these confirmed user accounts under **Supabase →
Authentication → Users**:

| Account  |         Email          |    Environment variable |
|:---------|:----------------------:|------------------------:|
| Patrick  | `patrick@example.com`  |  `SEED_PATRICK_AUTH_ID` |
| Nelson   |  `nelson@example.com`  |   `SEED_NELSON_AUTH_ID` |
| Danielle | `danielle@example.com` | `SEED_DANIELLE_AUTH_ID` |
| Okpara   |  `okpara@example.com`  |   `SEED_OKPARA_AUTH_ID` |

Copy each account's User UID into its corresponding value in `.env`

### 3. Start PostgreSQL

Start Docker Desktop, then run from the project root:

```bash
docker compose up -d
```

Check that the database is healthy:

```bash
docker compose ps
```

### 4. Configure Flyway

Copy the example configuration:

```bash
cp flyway.conf.example flyway.conf
```

In `flyway.conf`, set `flyway.password` to the same value as
`POSTGRES_PASSWORD` in `.env`.

Flyway does not automatically read `.env`. Keep both `.env` and
`flyway.conf` untracked; commit their example files instead.

The Flyway Maven plugin must point to the local configuration file:

```xml

<configuration>
    <configFiles>
        <configFile>${project.basedir}/flyway.conf</configFile>
    </configFiles>
</configuration>
```

### 5. Create IntelliJ run configurations

Open **Run → Edit Configurations** and create the following three
configurations.

#### Spring Boot Main App

Starts the backend application.

1. Click **+ → Spring Boot**.
2. Set **Name** to `Spring Boot Main App`.
3. Select the main application class annotated with
   `@SpringBootApplication`.
4. Select the application module under **Use classpath of module**.
5. Set **JRE** to the project’s Java 21 SDK.
6. Find **Environment variables**, under **Modify options** if hidden.
7. Add `POSTGRES_PASSWORD` and `SUPABASE_URL` with the same values used in
   `.env`.
8. Find Program arguments, under Modify options if hidden, and add:
   ```text
   --spring.profiles.active=dev
   ```

   The dev profile loads application-dev.properties and enables
   development seeding. To start without seeding, use:
      ```text
      --spring.profiles.active=dev --app.seed.enabled=false
      ```
9. Click **Apply**.

#### Flyway Info

Checks the database connection and displays applied and pending
migrations. It does not apply migrations.

1. Click **+ → Maven**.
2. Set **Name** to `Flyway Info`.
3. Set **Working directory** to the project root containing `pom.xml`.
4. Set **Run / Command line** to:

   ```text
   flyway:info
   ```

5. Ensure the Maven runner uses Java 21.
6. Click **Apply**.

#### Flyway Migrate

Applies pending database migrations.

1. Select the `Flyway Info` configuration.
2. Click **Copy Configuration**.
3. Change **Name** to `Flyway Migrate`.
4. Change **Run / Command line** to:

   ```text
   flyway:migrate
   ```

5. Click **Apply → OK**.

### 6. Run migrations

Store migration scripts in:

```text
src/main/resources/db/migration/
```

Use versioned filenames with two underscores between the version
and description:

```text
V1__create_farms.sql
V2__create_memberships.sql
```

With PostgreSQL running:

1. Select **Flyway Info** from IntelliJ’s run dropdown and click **Run**
   to check the connection and migration status.
2. Select **Flyway Migrate** and click **Run** to apply pending migrations.

Run **Flyway Migrate** again whenever new migration scripts are added.

### 7. Start the application

Select **Spring Boot Main App** from IntelliJ’s run dropdown and
click **Run**.

Confirm that the application starts successfully and connects to
PostgreSQL.

## Stop the Database

Run from the project root:

```bash
docker compose stop
```