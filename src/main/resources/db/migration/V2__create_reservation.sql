-- Proceso de reserva de un usuario final (maquina de estados + ids de la catedra).
-- El telefono y el nombre del paciente son datos personales: se guardan solo aca y nunca se loguean.
CREATE TABLE reservation (
    id                      VARCHAR(36)  PRIMARY KEY,
    user_login              VARCHAR(50)  NOT NULL,
    professional_id         BIGINT       NOT NULL,
    professional_name       VARCHAR(200) NOT NULL,
    appointment_date        DATE         NOT NULL,
    start_time              TIME         NOT NULL,
    end_time                TIME         NOT NULL,
    status                  VARCHAR(30)  NOT NULL,
    hold_id                 VARCHAR(100),
    catedra_process_id      VARCHAR(100),
    hold_expires_at         TIMESTAMPTZ,
    patient_first_name      VARCHAR(100),
    patient_last_name       VARCHAR(100),
    patient_phone           VARCHAR(30),
    request_event_id        VARCHAR(100),
    catedra_reservation_id  BIGINT,
    reason                  VARCHAR(100),
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    version                 BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_reservation_status CHECK (status IN ('HOLD_REQUESTED','HOLD_CREATED','CONFIRMATION_PENDING',
        'INFO_REQUESTED','INFO_SUBMITTED','INFO_REJECTED','CONFIRMED','CANCELLED','EXPIRED','FAILED','INVALID'))
);

-- Un usuario no puede tener dos procesos vivos sobre el mismo horario (idempotencia de CU-10)
CREATE UNIQUE INDEX uk_reservation_active_slot
    ON reservation (user_login, professional_id, appointment_date, start_time)
    WHERE status IN ('HOLD_REQUESTED','HOLD_CREATED','CONFIRMATION_PENDING','INFO_REQUESTED',
                     'INFO_SUBMITTED','INFO_REJECTED','CONFIRMED');

CREATE UNIQUE INDEX uk_reservation_catedra_process ON reservation (catedra_process_id)
    WHERE catedra_process_id IS NOT NULL;
CREATE INDEX ix_reservation_user ON reservation (user_login);
CREATE INDEX ix_reservation_status ON reservation (status);
