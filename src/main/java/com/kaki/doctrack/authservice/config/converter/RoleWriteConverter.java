package com.kaki.doctrack.authservice.config.converter;

import org.springframework.core.convert.converter.Converter;
import com.kaki.doctrack.authservice.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleWriteConverter implements Converter<Role, Long> {

    @Override
    public Long convert(Role role) {
        return role.getId();
    }
}
