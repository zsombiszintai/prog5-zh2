package hu.mik.pte.prog5.zh1vaadin.service;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;

public interface SecurityService {

    default List<String> getRolesAsString() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(Objects::toString)
                .map(s -> s.replace("ROLE_", "")).toList();
    }
}
