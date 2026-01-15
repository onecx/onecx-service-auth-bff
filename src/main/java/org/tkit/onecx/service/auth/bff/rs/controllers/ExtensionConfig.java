package org.tkit.onecx.service.auth.bff.rs.controllers;

import java.util.Map;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Extension configuration.
 */
@ConfigMapping(prefix = "onecx.extension")
public interface ExtensionConfig {

    /**
     * Profiles configuration.
     */
    @WithName("profiles")
    Map<String, Profile> profiles();

    /**
     * A-CID query parameter name.
     */
    @WithName("idmId-query-param")
    @WithDefault("idmId")
    String idmIdQueryParam();

    /**
     * Fallback profile
     */
    @WithName("fall-back-profile")
    Optional<String> fallBackProfile();

    /**
     * Profile configuration.
     */
    interface Profile {

        /**
         * SSO URL configuration.
         */
        @WithName("keycloak-url")
        String keycloakUrl();

        /**
         * IdmId
         */
        @WithName("idmId")
        String idmId();

        /**
         * idpHint
         */
        @WithName("idpHint")
        String idpHint();
    }
}
