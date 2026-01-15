package org.tkit.onecx.service.auth.bff.rs.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.service.auth.bff.test.AbstractTest;

import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigRequestDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(ExtensionRestController.class)
class ExtensionRestControllerProfileTest extends AbstractTest {

    @InjectMock
    ExtensionConfig extensionConfig;

    @Inject
    Config config;

    @Test
    void getConfigurationNoFallBackTest() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(ExtensionConfig.class);
        Mockito.when(extensionConfig.idmIdQueryParam()).thenReturn(tmp.idmIdQueryParam());
        Mockito.when(extensionConfig.fallBackProfile()).thenReturn(Optional.empty());
        Mockito.when(extensionConfig.profiles()).thenReturn(Map.of());
        given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href("https://dev-portal/ui?idmId=kc1").idmId("kc1"))
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getConfigurationNoProfileTest() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(ExtensionConfig.class);
        Mockito.when(extensionConfig.idmIdQueryParam()).thenReturn(tmp.idmIdQueryParam());
        Mockito.when(extensionConfig.fallBackProfile()).thenReturn(Optional.of("not-existing"));
        Mockito.when(extensionConfig.profiles()).thenReturn(Map.of());
        given()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(new ConfigRequestDTO().idpHint(null).href("https://dev-portal/ui?idmId=kc1").idmId("kc1"))
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
