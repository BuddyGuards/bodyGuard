package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.FoodLog;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.query.FoodLogWithFood;
import org.buddyguard.bodyguard.repository.FoodLogRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/food")
@AllArgsConstructor
public class FoodController {

    private FoodLogRepository foodLogRepository;

    @GetMapping("/diary")
    public String diaryHandle(Model model,
                              @RequestParam("eatenDate") @Nullable LocalDate eatenDate,
                              @SessionAttribute("user") User user) {


        eatenDate = eatenDate != null ? eatenDate : LocalDate.now();


        model.addAttribute("eatenDate", eatenDate);

        // ① 날짜별 전체 리스트
        List<FoodLogWithFood> dailyDiet =
                foodLogRepository.findWithFoodNameByUserIdAndEatenDate(eatenDate, user.getId());

        // ② 식사 타입별로 그룹핑
        Map<String, List<FoodLogWithFood>> meals = dailyDiet.stream()
                .collect(Collectors.groupingBy(FoodLogWithFood::getMealType));
        model.addAttribute("meals", meals);

        // ③ 식사별 총 칼로리 계산
        Map<String, Double> totalCalories = new HashMap<>();
        meals.forEach((meal, list) ->
                totalCalories.put(meal,
                        list.stream()
                                .mapToDouble(FoodLogWithFood::getKcal)
                                .sum()
                )
        );
        model.addAttribute("totalCalories", totalCalories);

        return "food/diary";
    }

    @PostMapping("/diary")
    public String diaryPostHandle(
            @ModelAttribute FoodLog foodLog,
            @SessionAttribute("user") User user
    ) {

        if (user == null) {
            // 세션이 없으면 로그인으로 이동
            return "redirect:/login";
        }

        FoodLog fl = FoodLog.builder()
                .userId(user.getId())
                .foodId(foodLog.getFoodId())
                .amount(foodLog.getAmount())
                .kcal(foodLog.getKcal())
                .eatenDate(foodLog.getEatenDate())
                .mealType(foodLog.getMealType())
                .build();
        foodLogRepository.save(fl);

        return "redirect:/food/diary";
    }
}
