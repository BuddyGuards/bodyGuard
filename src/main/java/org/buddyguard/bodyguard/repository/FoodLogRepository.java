package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.FoodLog;

@Mapper
public interface FoodLogRepository {

    public int save (FoodLog foodLog);
}
