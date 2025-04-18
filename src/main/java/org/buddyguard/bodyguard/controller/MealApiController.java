package org.buddyguard.bodyguard.controller;

import lombok.RequiredArgsConstructor;
import org.buddyguard.bodyguard.repository.MealRepository;
import org.buddyguard.bodyguard.service.MealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealApiController {

    private final MealRepository mealRepository;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable("id") Integer id) {
        int result = mealRepository.deleteMealById(id);
        if (result > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}