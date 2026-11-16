package com.integrador.turnos.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Column(nullable = false)
    private boolean activated;

    @Column(name = "lang_key", nullable = false, length = 10)
    private String langKey;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_authority", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority", length = 50)
    private Set<String> authorities = new HashSet<>();

    protected UserEntity() {
    }

    public UserEntity(Long id, String login, String passwordHash, String firstName, String lastName, String email,
                      String imageUrl, boolean activated, String langKey, Instant createdDate,
                      Instant lastModifiedDate, Set<String> authorities) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.activated = activated;
        this.langKey = langKey;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.authorities = new HashSet<>(authorities);
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActivated() { return activated; }
    public String getLangKey() { return langKey; }
    public Instant getCreatedDate() { return createdDate; }
    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public Set<String> getAuthorities() { return authorities; }
}
