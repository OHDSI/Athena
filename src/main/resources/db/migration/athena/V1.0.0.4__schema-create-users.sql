CREATE TABLE users (
  id         BIGINT PRIMARY KEY,
  login      VARCHAR(128) NOT NULL,
  origin     VARCHAR(128) NOT NULL DEFAULT 'Arachne',
  firstname  VARCHAR(255),
  middlename VARCHAR(255),
  lastname   VARCHAR(255),
  email      VARCHAR(255),
  CONSTRAINT users_login_origin_uq UNIQUE (LOGIN, origin)
);

CREATE TABLE roles (
  id          BIGINT PRIMARY KEY,
  name        VARCHAR(128) NOT NULL UNIQUE,
  description TEXT
);

CREATE TABLE users_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  CONSTRAINT users_roles_pk PRIMARY KEY (user_id, role_id),
  CONSTRAINT users_roles_users_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT users_roles_roles_fk FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE SEQUENCE users_seq START WITH 1000;

INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO users (id, login, origin, email) VALUES (1, 'admin@admin.ru', 'Arachne', 'admin@admin.ru');

INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (1, 2);