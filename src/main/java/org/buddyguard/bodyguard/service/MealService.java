package org.buddyguard.bodyguard.service;

import lombok.RequiredArgsConstructor;
import org.buddyguard.bodyguard.controller.MealApiController;
import org.buddyguard.bodyguard.repository.MealRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;

    public int deleteMealById(Integer id) {
        return mealRepository.deleteMealById(id);
    }

    // 나중에 확장 가능 예시
    // public void deleteMealWithLog(Integer id, String userId) {
    //     logService.write("식단 삭제: " + id);
    //     mealMapper.deleteMealById(id);
    // }
}