package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.Food;

import java.util.List;

@Mapper
public interface FoodRepository {

    public List<Food> searchFoodsByName (@Param("word") String word);
}
