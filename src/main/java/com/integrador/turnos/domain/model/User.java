package com.integrador.turnos.domain.model;

import java.time.Instant;
import java.util.Set;

/**
 * Usuario final. Compatible con el usuario de JHipster: login, email, nombre, imagen, idioma,
 * activacion, autoridades y fechas de auditoria. Guarda el HASH de la clave, nunca la clave.
 */
public class User {

    private final Long id;
    private final String login;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String imageUrl;
    private final String langKey;
    private final boolean activated;
    private final Set<String> authorities;
    private final Instant createdDate;

    public User(Long id, String login, String passwordHash, String firstName, String lastName, String email,
                String imageUrl, String langKey, boolean activated, Set<String> authorities, Instant createdDate) {
        if (login == null || login.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("login y passwordHash son obligatorios");
        }
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.langKey = langKey;
        this.activated = activated;
        this.authorities = Set.copyOf(authorities);
        this.createdDate = createdDate;
    }

    /** Alta de un usuario nuevo: activo de inmediato y con ROLE_USER, decididos por el backend. */
    public static User register(UserRegistration data, String passwordHash, Instant now) {
        return new User(null, data.login(), passwordHash, data.firstName(), data.lastName(), data.email(),
                data.imageUrl(), data.langKey(), true, Set.of(Authorities.USER), now);
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getImageUrl() { return imageUrl; }
    public String getLangKey() { return langKey; }
    public boolean isActivated() { return activated; }
    public Set<String> getAuthorities() { return authorities; }
    public Instant getCreatedDate() { return createdDate; }

    @Override
    public String toString() {
        return "User[id=" + id + ", login=" + login + ", activated=" + activated + ", passwordHash=***]";
    }
}
