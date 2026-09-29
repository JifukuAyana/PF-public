
package com.example.demo.controller.admin;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();

            if (categories.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("カテゴリーデータが見つかりません");
            }

            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("サーバーエラー: " + e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity<?> addCategory(@Valid @RequestBody Category category, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("カテゴリー名は255文字以内で入力してください");
        }
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }


    // カテゴリー削除用のエンドポイント
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category updatedCategory,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("カテゴリー名は255文字以内で入力してください");
        }

        Optional<Category> categoryOptional = categoryService.getCategoryById(id);
        if (!categoryOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("カテゴリーが見つかりません");
        }

        Category existingCategory = categoryOptional.get();
        existingCategory.setName(updatedCategory.getName());
        categoryService.saveCategory(existingCategory);

        return ResponseEntity.ok("カテゴリーを更新しました");
    }
}
