package org.buddyguard.bodyguard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/food")

public class FoodController {

    @GetMapping("/diary")
    public String diaryHandle(Model model) {

        return "food/diary";  // Thymeleaf 템플릿
    }
}
