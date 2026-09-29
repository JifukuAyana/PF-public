package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.repository.UserLikeHistoryRepository;

@Service
public class LikeService {

    private final UserLikeHistoryRepository likeHistoryRepo;

    public LikeService(UserLikeHistoryRepository likeHistoryRepo) {
        this.likeHistoryRepo = likeHistoryRepo;
    }

    // 🔹 年間ランキング（1月1日 ～ 現在）
    public List<String> getYearlyLikeRanking() {
        LocalDateTime startOfYear = LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0);
        LocalDateTime now = LocalDateTime.now();
        return likeHistoryRepo.findLikeRanking(startOfYear, now)
                .stream().map(record -> (String) record[0])
                .limit(5).toList();
    }

    // 🔹 月間ランキング（今月1日 ～ 現在）
    public List<String> getMonthlyLikeRanking() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return likeHistoryRepo.findLikeRanking(startOfMonth, now)
                .stream().map(record -> (String) record[0])
                .limit(5).toList();
    }
    
    public int getYearlyLikesCount(String loginId) {
        LocalDateTime startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return likeHistoryRepo.countByLoginIdAndLikedAtBetween(loginId, startOfYear, now);
    }

    public int getMonthlyLikesCount(String loginId) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return likeHistoryRepo.countByLoginIdAndLikedAtBetween(loginId, startOfMonth, now);
    }

    
//    公開サイトの月ごとのいいねランキング
    public List<String> getAvailableMonths() {
        return likeHistoryRepo.findDistinctMonths(); 
    }

    public List<Object[]> getMonthlyLikeRanking(String yearMonth) {
        return likeHistoryRepo.findMonthlyLikes(yearMonth); 
    }
    
    // 🔸 任意の期間のランキングを取得（例：月を選択して取得）
    public List<Object[]> getLikeRankingBetween(LocalDateTime start, LocalDateTime end) {
        return likeHistoryRepo.findLikeRanking(start, end);
    }

    
}

