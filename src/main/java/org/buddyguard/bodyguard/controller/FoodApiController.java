package org.buddyguard.bodyguard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.Food;
import org.buddyguard.bodyguard.repository.FoodRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/api/food")
@AllArgsConstructor
public class FoodApiController {

    private ObjectMapper objectMapper;
    private FoodRepository foodRepository;

    @GetMapping("/auto-complete")
    @ResponseBody
    public String autoComplete(@RequestParam("word") String word) throws JsonProcessingException {
        List<Food> list = foodRepository.searchFoodsByName("%" + word + "%");


        return objectMapper.writeValueAsString(list);
    }


    @GetMapping("/{foodId}/data")
    @ResponseBody
    public Food getFoodData(@PathVariable("foodId") int foodId) {
        return foodRepository.findById(foodId);
    }

}
