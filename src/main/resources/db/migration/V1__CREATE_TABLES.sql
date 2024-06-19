CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

CREATE TABLE users
(
    id                      BIGINT AUTO_INCREMENT NOT NULL,
    firstname               VARCHAR(100)          NOT NULL,
    lastname                VARCHAR(100)          NOT NULL,
    email                   VARCHAR(100)          NOT NULL,
    username                VARCHAR(100)          NOT NULL,
    phone                   VARCHAR(20)           NOT NULL,
    password                VARCHAR(255)          NOT NULL,
    account_non_expired     BIT(1)                NOT NULL,
    account_non_locked      BIT(1)                NOT NULL,
    credentials_non_expired BIT(1)                NOT NULL,
    enabled                 BIT(1)                NOT NULL,
    role_id                 BIGINT                NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_d41b903a377589c1f00330dcf UNIQUE (id, role_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_phone UNIQUE (phone);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);