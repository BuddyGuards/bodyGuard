package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;


@Mapper
public interface UserRepository {

    public int save(User user);

    public User findByEmail(String email);

    public void updateUserProfileByEmail(@Param("nickname") String nickname,
                                         @Param("imageUrl") String imageUrl,
                                         @Param("gender") String gender,
                                         @Param("height") double height,
                                         @Param("weight") double weight,
                                         @Param("age") int age,
                                         @Param("goal") String goal,
                                         @Param("email") String email);
}