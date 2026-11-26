package com.integrador.turnos.infrastructure.catedra;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Saca el "code" estable de un cuerpo application/problem+json sin depender de un parser JSON. */
public final class ProblemCodeExtractor {

    private static final Pattern CODE = Pattern.compile("\"code\"\\s*:\\s*\"([A-Z0-9_]+)\"");

    private ProblemCodeExtractor() {
    }

    /** @return el code o null si no hay cuerpo o no trae code */
    public static String extract(String body) {
        if (body == null) {
            return null;
        }
        Matcher m = CODE.matcher(body);
        return m.find() ? m.group(1) : null;
    }
}
