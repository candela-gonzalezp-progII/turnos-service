-- Usuarios finales de la app (contrato compatible con el usuario de JHipster).
-- La contrasena se guarda SOLO como hash BCrypt (60 caracteres); nunca en texto plano.
CREATE TABLE app_user (
    id                 BIGSERIAL PRIMARY KEY,
    login              VARCHAR(50)  NOT NULL,
    password_hash      VARCHAR(60)  NOT NULL,
    first_name         VARCHAR(50),
    last_name          VARCHAR(50),
    email              VARCHAR(254) NOT NULL,
    image_url          VARCHAR(256),
    activated          BOOLEAN      NOT NULL,
    lang_key           VARCHAR(10)  NOT NULL,
    created_date       TIMESTAMPTZ  NOT NULL,
    last_modified_date TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_app_user_login UNIQUE (login),
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

CREATE TABLE app_user_authority (
    user_id   BIGINT      NOT NULL REFERENCES app_user (id),
    authority VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, authority)
);
