package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final AuthEntity authEntity;

    public UserDetailsImpl(AuthEntity authEntity) {
        this.authEntity = authEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // 권한이 필요하면 설정
    }

    @Override
    public String getPassword() {
        return authEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return authEntity.getEmail(); // 또는 nickname
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
