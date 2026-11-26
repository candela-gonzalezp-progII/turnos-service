# turnos-service

Servicio de **usuarios, disponibilidad y reservas** del sistema distribuido de turnos
(Proyecto integrador Programación II — Candela González).

Registra y autentica usuarios finales (JWT), calcula los turnos libres combinando el catálogo con las ocupaciones
de la cátedra y gestiona el proceso de reserva completo contra la cátedra (REST + Kafka). Es dueño exclusivo de su
base de datos y usa catalogo-service solo por su API.

## Arquitectura

Hexagonal (puertos y adaptadores). El dominio es Java puro y no conoce Spring.

```
domain/model             Reservation (máquina de estados), Slot, PhoneNumber, eventos, reglas
application/port/in      casos de uso (CU-01 … CU-16)
application/port/out     catálogo, cátedra (holds, confirmación, citas), Kafka, base de datos
application/service      implementación de los casos de uso
infrastructure/catedra   cliente REST de la cátedra (token técnico, reintentos solo en lecturas)
infrastructure/kafka     consumidor de eventos, publicador de teléfono, decorador transaccional
infrastructure/persistence JPA + Flyway (Postgres), bloqueo optimista (@Version)
infrastructure/security  JWT HS256, política de acceso, token de servicio
infrastructure/recovery  recuperación al arrancar y periódica
infrastructure/web       controllers REST y manejo de errores (problem+json)
```

### Flujo de una reserva

```
HOLD_REQUESTED → HOLD_CREATED → CONFIRMATION_PENDING → INFO_REQUESTED → INFO_SUBMITTED → CONFIRMED → (CANCELLED)
                                                          ↘ INFO_REJECTED (se puede reenviar el teléfono)
Finales: CONFIRMED (solo puede cancelarse), CANCELLED, EXPIRED, FAILED, INVALID — un estado final no se reabre.
```

1. `POST /api/reservations` pide el hold a la cátedra (REST).
2. `POST /api/reservations/{id}/confirm` inicia la confirmación (REST, 202).
3. La cátedra pide el teléfono por Kafka → estado `INFO_REQUESTED`.
4. `POST /api/reservations/{id}/phone` publica el teléfono en Kafka.
5. La cátedra responde por Kafka (`CONFIRMED`, `INFO_REJECTED`, `EXPIRED`, `INVALID`).

Decisiones de diseño: eventos con receptor idempotente (`eventId`), una transacción por evento, los POST a la cátedra
nunca se reintentan a ciegas (se reconcilia por estado) y la recuperación converge los procesos pendientes.

## Requisitos

- Java 21
- Docker (Postgres local y tests de integración con Testcontainers)
- Acceso a la red de la cátedra para integrar contra el servicio real (no hace falta para correr los tests)

## Configuración

1. `cp .env.example .env` y completar:
   - `DB_*`: base de datos propia de turnos.
   - `JWT_SECRET`: mínimo 32 caracteres, **idéntico** al de catalogo-service (`openssl rand -base64 48`). Vacío = la app no arranca.
   - `CATEDRA_BASE_URL`, `CATEDRA_USERNAME`, `CATEDRA_PASSWORD`: los entrega la cátedra por otro canal (cuenta técnica única).
   - `CATEDRA_KAFKA_ENABLED=true` cuando la integración Kafka esté provisionada.
   - `CATALOGO_BASE_URL`: dirección de catalogo-service.
   - `CORS_ALLOWED_ORIGINS`: orígenes web permitidos (vacío = ninguno; la app Android no lo necesita).
2. `docker compose up -d` y `./gradlew bootRun`.
   Todo en contenedores: `docker compose --profile app up -d --build`.

El `.env` nunca se sube al repositorio.

## Tests

```
./gradlew test
```

Unitarios (dominio y casos de uso con repositorios en memoria) e integración con Postgres real (Testcontainers).

## Endpoints

| Método | Ruta | Caso de uso |
|---|---|---|
| POST | `/api/register` | CU-01 registrar usuario |
| POST | `/api/authenticate` | CU-02 iniciar sesión (JWT) |
| GET | `/api/account` | cuenta del usuario autenticado |
| GET | `/api/professionals/{id}/availability?date=YYYY-MM-DD` | CU-09 turnos libres |
| POST | `/api/reservations` | CU-10 bloquear horario (hold) |
| POST | `/api/reservations/{id}/confirm` | CU-11 confirmar por REST |
| POST | `/api/reservations/{id}/phone` | CU-12 enviar teléfono |
| GET | `/api/reservations/{id}` | estado de una reserva propia (CU-13) |
| GET | `/api/reservations` | CU-14 mis reservas |
| POST | `/api/reservations/{id}/cancel` | CU-15 cancelar reserva propia |

Errores en `application/problem+json` con `code` estable (`SLOT_ALREADY_HELD`, `HOLD_EXPIRED`,
`RESERVATION_NOT_FOUND`, `CATEDRA_UNAVAILABLE`, …).

## Estado del proyecto

| Caso de uso | Estado |
|---|---|
| CU-01 Registro · CU-02 Login JWT · CU-18 Token de servicio | implementado |
| CU-09 Disponibilidad con ocupaciones de la cátedra | implementado |
| CU-10 Hold · CU-11 Confirmación | implementado |
| CU-12 Teléfono (Kafka) · CU-13 Resultado final | implementado |
| CU-14 Mis reservas · CU-15 Cancelar | implementado |
| CU-16 Recuperación y errores | implementado |
