package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Category; // ✅ 必要
import com.example.demo.model.Inquiry;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.InquiryRepository;

@Service
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final CategoryRepository categoryRepository;

    public InquiryService(InquiryRepository inquiryRepository, CategoryRepository categoryRepository) {
        this.inquiryRepository = inquiryRepository;
        this.categoryRepository = categoryRepository; // ✅ 引数を追加して代入
    }

    public Inquiry saveInquiry(Inquiry inquiry) {
        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll();
    }

    public List<Inquiry> getInquiriesByCategory(String categoryName) {
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName); // ✅ String→Entity
        return categoryOpt.map(inquiryRepository::findByCategory).orElse(List.of());
    }

    public Optional<Inquiry> getInquiryById(Long id) {
        return inquiryRepository.findById(id);
    }

    public List<Inquiry> getInquiriesByCategoryName(String categoryName) {
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
        return categoryOpt.map(inquiryRepository::findByCategory).orElse(List.of());
    }

    public List<Inquiry> getInquiriesByCategoryId(Long id) {
        return inquiryRepository.findByCategoryId(id);
    }
}
