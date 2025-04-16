package org.buddyguard.bodyguard.entity;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodLog {
    private Integer id;              // PK
    private Integer userId;          // 사용자 ID (FK)
    private Integer foodId;          // 음식 ID (FK)
    private Double amount;           // 먹은 양 (단위: g)
    private Double kcal;             // 섭취한 총 칼로리
    private LocalDate eatenDate;     // 먹은 날짜
    private String mealType;       // 식사 유형 (아침/점심/저녁/간식)
}
