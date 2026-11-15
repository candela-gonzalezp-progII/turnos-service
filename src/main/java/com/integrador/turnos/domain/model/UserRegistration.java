package com.integrador.turnos.domain.model;

import com.integrador.turnos.domain.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Datos que puede elegir quien se registra (CU-01). Valida todos los campos juntos y, si hay errores,
 * informa cada uno. Id, activacion, autoridades y auditoria NO forman parte: los define el backend.
 * Login y email se normalizan a minusculas.
 */
public record UserRegistration(String login, String password, String firstName, String lastName,
                               String email, String imageUrl, String langKey) {

    public static final String DEFAULT_LANG = "es";
    private static final Pattern LOGIN = Pattern.compile("^[_.@A-Za-z0-9-]+$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static UserRegistration of(String login, String password, String firstName, String lastName,
                                      String email, String imageUrl, String langKey) {
        List<FieldError> errors = new ArrayList<>();
        String normalizedLogin = login == null ? null : login.trim().toLowerCase(Locale.ROOT);
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);

        checkLogin(normalizedLogin, errors);
        checkPassword(password, errors);
        checkEmail(normalizedEmail, errors);
        checkMax("firstName", firstName, 50, errors);
        checkMax("lastName", lastName, 50, errors);
        checkMax("imageUrl", imageUrl, 256, errors);
        String lang = langKey == null || langKey.isBlank() ? DEFAULT_LANG : langKey.trim();
        if (lang.length() < 2 || lang.length() > 10) {
            errors.add(new FieldError("langKey", "debe tener entre 2 y 10 caracteres"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new UserRegistration(normalizedLogin, password, blankToNull(firstName), blankToNull(lastName),
                normalizedEmail, blankToNull(imageUrl), lang);
    }

    private static void checkLogin(String login, List<FieldError> errors) {
        if (login == null || login.isEmpty()) {
            errors.add(new FieldError("login", "es obligatorio"));
        } else if (login.length() < 3 || login.length() > 50) {
            errors.add(new FieldError("login", "debe tener entre 3 y 50 caracteres"));
        } else if (!LOGIN.matcher(login).matches()) {
            errors.add(new FieldError("login", "solo admite letras, numeros y . _ @ -"));
        }
    }

    private static void checkPassword(String password, List<FieldError> errors) {
        if (password == null || password.isEmpty()) {
            errors.add(new FieldError("password", "es obligatoria"));
        } else if (password.length() < 4 || password.length() > 100) {
            errors.add(new FieldError("password", "debe tener entre 4 y 100 caracteres"));
        }
    }

    private static void checkEmail(String email, List<FieldError> errors) {
        if (email == null || email.isEmpty()) {
            errors.add(new FieldError("email", "es obligatorio"));
        } else if (email.length() < 5 || email.length() > 254 || !EMAIL.matcher(email).matches()) {
            errors.add(new FieldError("email", "no es un email valido (5 a 254 caracteres)"));
        }
    }

    private static void checkMax(String field, String value, int max, List<FieldError> errors) {
        if (value != null && value.length() > max) {
            errors.add(new FieldError(field, "no puede superar " + max + " caracteres"));
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** La clave nunca sale en un log. */
    @Override
    public String toString() {
        return "UserRegistration[login=" + login + ", email=" + email + ", password=***]";
    }
}
