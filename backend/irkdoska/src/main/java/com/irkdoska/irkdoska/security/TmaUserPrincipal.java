package com.irkdoska.irkdoska.security;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class TmaUserPrincipal implements UserDetails, Principal {
    private String initRawData;
    private Long telegramId;
    private String firstName;
    private String lastName;
    private String username;
    private String role;

    public TmaUserPrincipal(String initRawData, Long telegramId, String firstName, String lastName, String username, String role) {
        this.initRawData = initRawData;
        this.telegramId = telegramId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
    }

    public String getInitRawData() {
        return initRawData;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
    

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return telegramId.toString();
    }
}
