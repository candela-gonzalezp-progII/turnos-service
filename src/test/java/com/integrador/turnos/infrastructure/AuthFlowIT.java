package com.integrador.turnos.infrastructure;

import com.integrador.turnos.AbstractPostgresIT;
import com.integrador.turnos.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo con Postgres real: registrar -> autenticar -> consultar cuenta con el token. */
class AuthFlowIT extends AbstractPostgresIT {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(jwtFilter.getFilter()).build();
    }

    private static final String REGISTER = """
            {"login":"flow_user","password":"clave1234","email":"flow@mail.com","firstName":"Flow"}""";

    @Test
    void registrarAutenticarYConsultarCuenta() throws Exception {
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isCreated());

        String body = mvc.perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"flow_user\",\"password\":\"clave1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").exists())
                .andReturn().getResponse().getContentAsString();
        String token = body.replaceAll(".*\"id_token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/account").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("flow_user"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registroDuplicadoDa409() throws Exception {
        String json = "{\"login\":\"dup_user\",\"password\":\"clave1234\",\"email\":\"dup@mail.com\"}";
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated());

        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOGIN_ALREADY_USED"));
    }

    @Test
    void datosInvalidosDan400ConDetallePorCampo() throws Exception {
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"a\",\"password\":\"1\",\"email\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("login"));
    }

    @Test
    void claveIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nadie\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void cuentaSinTokenDa401() throws Exception {
        mvc.perform(get("/api/account")).andExpect(status().isUnauthorized());
    }
}
