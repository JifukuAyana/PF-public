package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Category;
import com.example.demo.model.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
	
	  Optional<Inquiry> findById(Long id);
    
    // カテゴリごとの問い合わせを取得
//	List<Inquiry> findByCategory(String category); 
	  List<Inquiry> findByCategory(Category category);

    // ページネーション対応
    Page<Inquiry> findAll(Pageable pageable);
    
    List<Inquiry> findByCategoryId(Long categoryId);
    
   
}


