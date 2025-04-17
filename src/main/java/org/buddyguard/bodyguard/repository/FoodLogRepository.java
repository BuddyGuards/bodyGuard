package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.FoodLog;
import org.buddyguard.bodyguard.query.FoodLogWithFood;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FoodLogRepository {

    public int save (FoodLog foodLog);

    public List<FoodLogWithFood> findWithFoodNameByEatenDate (LocalDate eatenDate);
    public List<FoodLogWithFood> findWithFoodNameByUserIdAndEatenDate (@Param("eatenDate") LocalDate eatenDate,
                                                                       @Param("userId") int userId);
}
