package com.questboard.security;

import com.questboard.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean enabled;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

    public CustomUserDetails(UUID id, String username, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.role = role;
        this.enabled = enabled;
    }

    public User toDomain() {
        // Não usamos isso para login; mantido apenas como conveniência.
        throw new UnsupportedOperationException("Use UserService.require(id)");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
}