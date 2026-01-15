package org.tkit.onecx.service.auth.bff.rs.controllers;

import java.net.URI;
import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.service.auth.bff.rs.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.extensions.bff.rs.internal.ExtensionsInternalApiService;
import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigRequestDTO;
import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigResponseDTO;
import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ExtensionRestController implements ExtensionsInternalApiService {

    private static final Logger log = LoggerFactory.getLogger(ExtensionRestController.class);

    @Inject
    ExtensionConfig config;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getConfiguration(ConfigRequestDTO configRequestDTO) {

        URI uri;
        try {
            uri = URI.create(configRequestDTO.getHref());
        } catch (Exception ex) {
            log.error("Wrong format of the href parameter. Request parameter href: '{}'", configRequestDTO.getHref());
            return exceptionMapper.badRequest("Bad request data");
        }

        // check query parameter for idmId
        var idmId = getQueryIdmId(uri.getQuery());
        if (idmId == null) {
            idmId = configRequestDTO.getIdmId();
        }

        // find profile name base of the proxy prefix
        var name = findProfileName(idmId);

        // if not name is found and not fall-back profile is defined
        if (name == null) {
            log.error("No configuration profile name found for host '{}'", uri);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // check if name of the profile exists
        // this could happen only if fall-back profile name is wrong defined
        var profile = config.profiles().get(name);
        if (profile == null) {
            log.error("No configuration profile found for name '{}'", name);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String idpHint = profile.idpHint();

        return Response
                .ok(new ConfigResponseDTO().url(profile.keycloakUrl()).idpHint(idpHint).idmId(idmId))
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    private String getQueryIdmId(String query) {
        if (query != null) {
            var queryValue = Arrays.stream(query.split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(s -> config.idmIdQueryParam().equals(s[0]))
                    .map(s -> s[1]).findFirst();

            if (queryValue.isPresent()) {
                return queryValue.get();
            }
        }
        return null;
    }

    private String findProfileName(String idmId) {
        // set fall-back profile as default profile
        var name = config.fallBackProfile().orElse(null);

        // find profile name base of the proxy prefix
        for (var item : config.profiles().entrySet()) {
            if (idmId != null && idmId.startsWith(item.getValue().idmId())) {
                name = item.getKey();
            }
        }
        return name;
    }
}
