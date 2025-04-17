package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.FoodLog;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.FoodLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/food")
@AllArgsConstructor
public class FoodController {

    private FoodLogRepository foodLogRepository;

    @GetMapping("/diary")
    public String diaryHandle(Model model) {

        return "food/diary";  // Thymeleaf 템플릿
    }

    @PostMapping("/diary")
    public String diaryPostHandle (@ModelAttribute FoodLog foodLog,
                                   @SessionAttribute("user") User user){

        FoodLog fl = FoodLog.builder().userId(user.getId())
                .foodId(foodLog.getFoodId())
                .amount(foodLog.getAmount())
                .kcal(foodLog.getKcal())
                .eatenDate(foodLog.getEatenDate())
                .mealType(foodLog.getMealType()).build();


        foodLogRepository.save(fl);

        return "redirect:/food/diary";
    }

}
