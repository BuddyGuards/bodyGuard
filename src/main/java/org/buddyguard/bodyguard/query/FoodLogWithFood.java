package org.buddyguard.bodyguard.query;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class FoodLogWithFood {
    // ---------------- food_logs 컬럼 ----------------
    private Integer   id;
    private Integer   userId;
    private Integer   foodId;
    private Double amount;
    private Double kcal;           // food_logs 쪽 kcal
    private LocalDate eatenDate;
    private String mealType;

    // ---------------- foods 컬럼 (id는 foodId 로 이미 매핑) ----------------
    private String name;
    private String foodGroup;
    private String representativeFood;
    private String nutrientReferenceAmount;
    private Double proteinG;
    private Double fatG;
    private Double carbohydrateG;
    private Double sugarG;
    private Double fiberG;
    private Double calciumMg;
    private Double ironMg;
    private Double phosphorusMg;
    private Double potassiumMg;
    private Double sodiumMg;
    private Double vitaminARae;
    private Double retinolUg;
    private Double betaCaroteneUg;
    private Double thiaminMg;
    private Double riboflavinMg;
    private Double niacinMg;
    private Double vitaminCMg;
    private Double vitaminDUg;
    private Double cholesterolMg;
    private Double saturatedFatG;
    private Double transFatG;
}
