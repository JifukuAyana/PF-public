package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.UserLikeHistory;

public interface UserLikeHistoryRepository extends JpaRepository<UserLikeHistory, Long> {

    // 🔹 いいね履歴の集計（任意の期間）
    @Query("""
        SELECT ulh.loginId, COUNT(ulh.id)
        FROM UserLikeHistory ulh
        WHERE ulh.likedAt BETWEEN :start AND :end
        GROUP BY ulh.loginId
        ORDER BY COUNT(ulh.id) DESC
    """)
    List<Object[]> findLikeRanking(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 🔹 ログインユーザーのいいね件数（期間指定）
    int countByLoginIdAndLikedAtBetween(String loginId, LocalDateTime start, LocalDateTime end);

    // 🔹 登録されている「年月」の一覧（例: "2025-03", "2025-04"）
    @Query("""
        SELECT DISTINCT DATE_FORMAT(ulh.likedAt, '%Y-%m')
        FROM UserLikeHistory ulh
        ORDER BY DATE_FORMAT(ulh.likedAt, '%Y-%m') DESC
    """)
    List<String> findDistinctMonths();

    // 🔹 指定された年月のランキング（例: "2025-04"）
    @Query("""
        SELECT ulh.loginId, COUNT(ulh.id)
        FROM UserLikeHistory ulh
        WHERE DATE_FORMAT(ulh.likedAt, '%Y-%m') = :yearMonth
        GROUP BY ulh.loginId
        ORDER BY COUNT(ulh.id) DESC
    """)
    List<Object[]> findMonthlyLikes(@Param("yearMonth") String yearMonth);
    
    
 // 🔸 loginIdを一括更新（loginId変更対応用）
    @Modifying
    @Query(value = "UPDATE user_like_history SET login_id = :newId WHERE login_id = :oldId", nativeQuery = true)
    void updateLoginId(@Param("oldId") String oldId, @Param("newId") String newId);
    
//    アカウント削除の際にいいね履歴を削除する。
    @Modifying
    @Transactional
    @Query("DELETE FROM UserLikeHistory u WHERE u.loginId = :loginId")
    void deleteByLoginId(@Param("loginId") String loginId);
}