package com.integrador.turnos.infrastructure.catedra;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemCodeExtractorTest {

    @Test
    void extraeElCodeDelProblemJson() {
        assertThat(ProblemCodeExtractor.extract("{\"status\":409,\"detail\":\"x\",\"code\": \"SLOT_ALREADY_HELD\"}"))
                .isEqualTo("SLOT_ALREADY_HELD");
    }

    @Test
    void sinCodeDevuelveNull() {
        assertThat(ProblemCodeExtractor.extract("{\"status\":403}")).isNull();
        assertThat(ProblemCodeExtractor.extract(null)).isNull();
        assertThat(ProblemCodeExtractor.extract("")).isNull();
    }
}
