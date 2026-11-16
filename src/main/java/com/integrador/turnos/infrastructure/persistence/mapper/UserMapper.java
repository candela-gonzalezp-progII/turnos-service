package com.integrador.turnos.infrastructure.persistence.mapper;

import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.infrastructure.persistence.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getLogin(), e.getPasswordHash(), e.getFirstName(), e.getLastName(),
                e.getEmail(), e.getImageUrl(), e.getLangKey(), e.isActivated(), e.getAuthorities(), e.getCreatedDate());
    }

    public static UserEntity toEntity(User u) {
        return new UserEntity(u.getId(), u.getLogin(), u.getPasswordHash(), u.getFirstName(), u.getLastName(),
                u.getEmail(), u.getImageUrl(), u.isActivated(), u.getLangKey(), u.getCreatedDate(),
                u.getCreatedDate(), u.getAuthorities());
    }
}
