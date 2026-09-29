package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    
    // 🔹 削除されていないユーザーを取得
    List<UserInfo> findByDeletedFalse();

    // 🔹 削除されたユーザーを取得
    List<UserInfo> findByDeletedTrue();
    
 // 🔸 削除されていないユーザーを「likes」降順で取得
    List<UserInfo> findByDeletedFalseOrderByLikesDesc();


    Optional<UserInfo> findByLoginId(String loginId);
	
    @Modifying
    @Transactional
    @Query("DELETE FROM UserInfo u WHERE u.loginId = :loginId")
    void deleteByLoginId(@Param("loginId") String loginId);
    
    
}
