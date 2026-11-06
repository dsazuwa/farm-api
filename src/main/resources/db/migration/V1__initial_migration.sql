CREATE TABLE users
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    auth_user_id UUID         NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_email_not_blank CHECK (LENGTH(TRIM(email)) > 0),
    CONSTRAINT users_email_trimmed CHECK (email = TRIM(email))
);

CREATE UNIQUE INDEX uq_users_email ON users (LOWER(email));


CREATE TABLE farms
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    city        VARCHAR(50),
    state       VARCHAR(50),
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT farms_name_not_blank CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT farms_name_trimmed CHECK (name = TRIM(name))
);


CREATE TABLE memberships
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL,
    farm_id             UUID        NOT NULL,
    role                VARCHAR(20) NOT NULL,
    sector_access_scope VARCHAR(20) NOT NULL DEFAULT 'selected',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT memberships_user_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT memberships_farm_fk FOREIGN KEY (farm_id) REFERENCES farms (id),

    CONSTRAINT memberships_farm_user_unique UNIQUE (farm_id, user_id),
    CONSTRAINT memberships_id_farm_unique UNIQUE (id, farm_id),

    CONSTRAINT memberships_role_valid
        CHECK (role IN (
                        'owner',
                        'manager',
                        'accountant',
                        'attendant',
                        'viewer'
            )),

    CONSTRAINT memberships_access_scope_valid
        CHECK (sector_access_scope IN ('all', 'selected')),

    CONSTRAINT memberships_owner_access
        CHECK (role <> 'owner' OR sector_access_scope = 'all')
);

CREATE INDEX memberships_user_id_idx ON memberships (user_id);


CREATE TABLE sectors
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    farm_id     UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT sectors_farms_fk FOREIGN KEY (farm_id) REFERENCES farms (id),

    CONSTRAINT sectors_id_farm_unique UNIQUE (id, farm_id),
    CONSTRAINT sectors_name_not_blank CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT sectors_name_trimmed CHECK (name = TRIM(name)),

    CONSTRAINT sectors_type_valid
        CHECK (type IN (
                        'layers',
                        'broilers',
                        'piggery',
                        'fishery',
                        'crops',
                        'general'
            ))
);

CREATE INDEX sectors_farm_id_idx ON sectors (farm_id);


CREATE TABLE membership_sector_access
(
    membership_id UUID NOT NULL,
    sector_id     UUID NOT NULL,
    farm_id       UUID NOT NULL,

    PRIMARY KEY (membership_id, sector_id),

    CONSTRAINT membership_sector_access_membership_fk
        FOREIGN KEY (membership_id, farm_id)
            REFERENCES memberships (id, farm_id)
            ON DELETE CASCADE,

    CONSTRAINT membership_sector_access_sector_fk
        FOREIGN KEY (sector_id, farm_id)
            REFERENCES sectors (id, farm_id)
            ON DELETE CASCADE
);

CREATE INDEX membership_sector_access_sector_farm_idx
    ON membership_sector_access (sector_id, farm_id);


CREATE TABLE workers
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    farm_id     UUID         NOT NULL,
    user_id     UUID,
    full_name   VARCHAR(150) NOT NULL,
    started_on  DATE         NOT NULL DEFAULT CURRENT_DATE,
    ended_on    DATE,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT workers_farm_fk FOREIGN KEY (farm_id) REFERENCES farms (id),
    CONSTRAINT workers_user_fk FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT workers_farm_user_unique UNIQUE (farm_id, user_id),
    CONSTRAINT workers_id_farm_unique UNIQUE (id, farm_id),

    CONSTRAINT workers_full_name_not_blank
        CHECK (LENGTH(TRIM(full_name)) > 0),

    CONSTRAINT workers_full_name_trimmed
        CHECK (full_name = TRIM(full_name)),

    CONSTRAINT workers_dates_valid
        CHECK (ended_on IS NULL OR ended_on >= started_on)
);

CREATE INDEX workers_user_id_idx ON workers (user_id);


CREATE TABLE worker_sector_assignments
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    worker_id      UUID        NOT NULL,
    sector_id      UUID        NOT NULL,
    farm_id        UUID        NOT NULL,
    assigned_from  DATE        NOT NULL DEFAULT CURRENT_DATE,
    assigned_until DATE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT worker_sector_assignments_worker_fk
        FOREIGN KEY (worker_id, farm_id)
            REFERENCES workers (id, farm_id),

    CONSTRAINT worker_sector_assignments_sector_fk
        FOREIGN KEY (sector_id, farm_id)
            REFERENCES sectors (id, farm_id),

    CONSTRAINT worker_sector_assignments_start_unique
        UNIQUE (worker_id, sector_id, assigned_from),

    CONSTRAINT worker_sector_assignments_dates_valid
        CHECK (
            assigned_until IS NULL
                OR assigned_until >= assigned_from
            )
);

CREATE INDEX worker_sector_assignments_sector_farm_idx
    ON worker_sector_assignments (sector_id, farm_id);

CREATE INDEX worker_sector_assignments_farm_id_idx
    ON worker_sector_assignments (farm_id);

CREATE UNIQUE INDEX worker_sector_assignments_open_unique
    ON worker_sector_assignments (worker_id, sector_id) WHERE assigned_until IS NULL;


CREATE TABLE jobs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    farm_id     UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT jobs_farm_fk
        FOREIGN KEY (farm_id) REFERENCES farms (id),

    CONSTRAINT jobs_id_farm_unique
        UNIQUE (id, farm_id),

    CONSTRAINT jobs_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0),

    CONSTRAINT jobs_name_trimmed
        CHECK (name = TRIM(name))
);

CREATE INDEX jobs_farm_id_idx ON jobs (farm_id);


CREATE TABLE worker_job_assignments
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    worker_id  UUID        NOT NULL,
    job_id     UUID        NOT NULL,
    farm_id    UUID        NOT NULL,
    started_on DATE        NOT NULL DEFAULT CURRENT_DATE,
    ended_on   DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT worker_job_assignments_worker_fk
        FOREIGN KEY (worker_id, farm_id)
            REFERENCES workers (id, farm_id),

    CONSTRAINT worker_job_assignments_job_fk
        FOREIGN KEY (job_id, farm_id)
            REFERENCES jobs (id, farm_id),

    CONSTRAINT worker_job_assignments_start_unique
        UNIQUE (worker_id, job_id, started_on),

    CONSTRAINT worker_job_assignments_dates_valid
        CHECK (ended_on IS NULL OR ended_on >= started_on)
);

CREATE INDEX worker_job_assignments_job_farm_idx
    ON worker_job_assignments (job_id, farm_id);

CREATE INDEX worker_job_assignments_farm_id_idx
    ON worker_job_assignments (farm_id);

CREATE UNIQUE INDEX worker_job_assignments_open_unique
    ON worker_job_assignments (worker_id, job_id) WHERE ended_on IS NULL;