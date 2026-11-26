package com.integrador.turnos.infrastructure.web;

public record SubmitPhoneRequest(String phoneNumber) {

    /** El telefono es un dato personal: nunca sale en logs ni en toString. */
    @Override
    public String toString() {
        return "SubmitPhoneRequest[***]";
    }
}
