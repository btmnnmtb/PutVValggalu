package com.example.demo.Config;

import com.example.demo.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsConfig  implements UserDetails {
    private final User user;
    public UserDetailsConfig(User user) { this.user = user; }

    @Override
    public String getUsername() { return user.getLogin(); }

    @Override
    public String getPassword() { return user.getPasswordHash(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getRoleName()));
    }
    @Override public boolean isEnabled() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isAccountNonExpired() { return true; }
}

