package org.buddyguard.bodyguard.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Food {
    private Integer id;  // 아이디
    private String name;  // 식품명
    private String foodGroup;  // 식품대분류명
    private String representativeFood;  // 대표식품명
    private String nutrientReferenceAmount;  // 영양성분함량기준량

    private Double kcal;  // 에너지(kcal)
    private Double proteinG;  // 단백질(g)
    private Double fatG;  // 지방(g)
    private Double carbohydrateG;  // 탄수화물(g)
    private Double sugarG;  // 당류(g)
    private Double fiberG;  // 식이섬유(g)
    private Double calciumMg;  // 칼슘(mg)
    private Double ironMg;  // 철(mg)
    private Double phosphorusMg;  // 인(mg)
    private Double potassiumMg;  // 칼륨(mg)
    private Double sodiumMg;  // 나트륨(mg)
    private Double vitaminARae;  // 비타민 A(μg RAE)
    private Double retinolUg;  // 레티놀(μg)
    private Double betaCaroteneUg;  // 베타카로틴(μg)
    private Double thiaminMg;  // 티아민(mg)
    private Double riboflavinMg;  // 리보플라빈(mg)
    private Double niacinMg;  // 니아신(mg)
    private Double vitaminCMg;  // 비타민 C(mg)
    private Double vitaminDUg;  // 비타민 D(μg)
    private Double cholesterolMg;  // 콜레스테롤(mg)
    private Double saturatedFatG;  // 포화지방산(g)
    private Double transFatG;  // 트랜스지방산(g)

}
