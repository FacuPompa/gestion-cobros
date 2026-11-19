CREATE TABLE app_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,

    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uq_app_users_username UNIQUE (username),
    CONSTRAINT ck_app_users_role CHECK (role IN ('OPERATOR', 'ADMIN'))
);
