package com.example.demo.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryViewController {

    @GetMapping("/category")
    public String categoryPage() {
        return "admin/category";  // `templates/admin/category.html` を返す
    }
}
