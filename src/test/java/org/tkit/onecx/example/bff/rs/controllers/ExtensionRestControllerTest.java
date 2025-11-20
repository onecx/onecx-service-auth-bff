package org.tkit.onecx.example.bff.rs.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.example.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.example.bff.test.AbstractTest;

import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigRequestDTO;
import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigResponseDTO;
import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ExtensionRestController.class)
class ExtensionRestControllerTest extends AbstractTest {

    private static Stream<Arguments> generateHrefTestData() {
        return Stream.of(
                arguments("https://dev-portal/ui?idmId=default", "default"),
                arguments("https://dev-portal/ui?param=xxx", "kc1"));
    }

    @ParameterizedTest
    @MethodSource("generateHrefTestData")
    void getConfigurationIntranetTest(String href, String idmId) {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href(href).idmId(idmId))
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ConfigResponseDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getIdpHint()).isEqualTo("100");
    }

    @Test
    void getConfigurationNoIdmIdTestShouldUseFallback() {
        given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href("https://dev-portal-proxy/ui"))
                .post()
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void getConfigurationIntranetNullHrefTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href(null))
                .post()
                .then()
                .log().all()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getErrorCode()).isNotNull()
                .isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(dto.getDetail()).isNotNull().isEqualTo("getConfiguration.configRequestDTO.href: must not be null");
    }

    @Test
    void getConfigurationIntranetWrongHrefTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href("htttp://wrong\\.com"))
                .post()
                .then()
                .log().all()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getErrorCode()).isNotNull()
                .isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(dto.getDetail()).isNotNull().isEqualTo("Bad request data");
    }

    @Test
    void getConfigurationEmptyRequestTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getErrorCode()).isNotNull()
                .isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(dto.getDetail()).isNotNull().isEqualTo("getConfiguration.configRequestDTO: must not be null");
    }

    @Test
    void getConfigurationWrongIdmIdTest_should_use_fallback() {

        given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href("https://not-existing-proxy.test.com/ui?idmId=1000"))
                .post()
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void getConfigurationAcidTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).idmId("kc1").href("https://portal-proxy/ui"))
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ConfigResponseDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getIdpHint()).isEqualTo("100");
    }
}
