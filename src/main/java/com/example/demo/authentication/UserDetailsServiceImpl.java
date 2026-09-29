package com.example.demo.authentication;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.demo.constant.MessageConst;
import com.example.demo.repository.UserInfoRepository;

import lombok.RequiredArgsConstructor;

/**
 * ユーザー情報生成
 * @author ayyyy
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * ユーザー情報テーブルRepository
     */
    private final UserInfoRepository repository;

    /**
     * ユーザー情報生成
     * @param username ログインID
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userInfo = repository.findById(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        
        //  「deleted」がtrue（= 1）の場合は例外スローしてログイン失敗扱いにする
        if (userInfo.isDeleted()) {
            throw new DisabledException(MessageConst.LOGIN_DELETED_USER);
        }

        String authority = userInfo.getAuthority();
        if (authority == null || authority.isBlank()) {
            authority = "ROLE_USER"; // デフォルト値
        }

        return User.withUsername(userInfo.getLoginId())
            .password(userInfo.getPassword())
            .authorities(authority)
            .build();
    }

}
