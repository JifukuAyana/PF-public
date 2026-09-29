package com.example.demo.entity;

import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Entity
@Table(name = "user_info")
@Data
public class UserInfo implements UserDetails {

    @Id
    @Column(name = "login_id")
    private String loginId;

    private String password;

    @Column(name = "mail")
    private String mail;

    @Column(name = "authority")
    private String authority;

    @Column(name = "access")
    private int access;

    @Column(name = "furigana")
    private String furigana;

    @Column(name = "gender")
    private int gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "introduction")
    private String introduction;

    private String profileImage;

    @Column(name = "deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted = false;

    @Column(name = "likes")
    private int likes;

    // ✅ Spring Security 用のオーバーライド
    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + authority);
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