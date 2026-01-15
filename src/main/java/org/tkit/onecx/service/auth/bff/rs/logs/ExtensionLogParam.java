package org.tkit.onecx.service.auth.bff.rs.logs;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.extensions.bff.rs.internal.model.ConfigRequestDTO;

@ApplicationScoped
public class ExtensionLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(

                item(10, ConfigRequestDTO.class,
                        x -> ConfigRequestDTO.class.getSimpleName() + ":"
                                + ((ConfigRequestDTO) x).getHref()));
    }
}
