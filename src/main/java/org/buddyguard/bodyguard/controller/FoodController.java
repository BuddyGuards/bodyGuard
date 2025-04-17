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
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map.Entry;

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


    @GetMapping("/report")
    public String reportHandle (Model model,
                                @RequestParam(value="eatenDate", required=false) LocalDate eatenDate,
                                @SessionAttribute("user") User user){

        // 1. 날짜 기본값
        LocalDate date = (eatenDate != null ? eatenDate : LocalDate.now());
        model.addAttribute("date", date);

        // 2. 일일 식단 불러오기
        List<FoodLogWithFood> dailyDiet =
                foodLogRepository.findWithFoodNameByUserIdAndEatenDate(date, user.getId());

        // 3. 실제 섭취량 합산
        Map<String, Double> actual = new LinkedHashMap<>();
        actual.put("calories", dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getKcal)
                .sum());
        actual.put("carbs",    dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getCarbohydrateG)
                .sum());
        actual.put("protein",  dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getProteinG)
                .sum());
        actual.put("fat",      dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getFatG)
                .sum());
        actual.put("sugar",    dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getSugarG)
                .sum());
        actual.put("sodium",   dailyDiet.stream()
                .mapToDouble(FoodLogWithFood::getSodiumMg)
                .sum());

        model.addAttribute("actual", actual);

        // 4. 권장 섭취량 (한국인 20대 남자 기준)
        Map<String, Double> recommended = new LinkedHashMap<>();
        recommended.put("calories", 2600.0);
        recommended.put("carbs",     130.0);
        recommended.put("protein",    65.0);
        recommended.put("fat",        87.0);
        recommended.put("sugar",      65.0);
        recommended.put("sodium",   2300.0);
        model.addAttribute("recommended", recommended);


        // 5. 달성률(%) 계산
        Map<String, Double> percent = new LinkedHashMap<>();
        for (String key : actual.keySet()) {
            double a = actual.get(key);
            double r = recommended.getOrDefault(key, 0.0);
            percent.put(key, (r > 0 ? (a / r * 100.0) : 0.0));
        }
        model.addAttribute("percent", percent);


        // 5. actual 엔트리 리스트로 만들어 넘기기
        List<Map.Entry<String, Double>> actualEntries = new ArrayList<>(actual.entrySet());
        model.addAttribute("actualEntries", actualEntries);

        // 6. JS용 리스트 추가
        List<String> keys = List.of("calories", "carbs", "protein", "fat", "sugar", "sodium");
        List<Double> actualList = keys.stream().map(actual::get).toList();
        List<Double> recommendedList = keys.stream().map(recommended::get).toList();

        model.addAttribute("actualList", actualList);
        model.addAttribute("recommendedList", recommendedList);


        //비율로 변환해서 넘기기
        List<Double> radarData = new ArrayList<>();
        for (String key : keys) {
            double actualValue = actual.getOrDefault(key, 0.0);
            double recommendedValue = recommended.getOrDefault(key, 1.0); // 0 방지
            radarData.add((actualValue / recommendedValue) * 100);
        }
        model.addAttribute("radarData", radarData);

        return "food/report";
    }



}
