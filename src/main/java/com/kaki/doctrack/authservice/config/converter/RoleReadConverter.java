package com.kaki.doctrack.authservice.config.converter;

import com.kaki.doctrack.authservice.entity.ERole;
import org.springframework.core.convert.converter.Converter;
import com.kaki.doctrack.authservice.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleReadConverter implements Converter<Long, Role> {

    @Override
    public Role convert(Long roleId) {
        // The actual conversion logic will be handled elsewhere
        Role role = new Role();
        role.setId(roleId);
        role.setName(ERole.nameFromId(roleId.intValue()));
        return role;
    }
}
