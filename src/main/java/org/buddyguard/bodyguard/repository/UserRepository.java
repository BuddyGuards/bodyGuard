package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;


@Mapper
public interface UserRepository {

    public int save(User user);

    public User findByEmail(String email);


    public User findById (int Id);

  
    public User findByProviderAndProviderId(@Param("provider") String provider,
                                            @Param("providerId") String providerId);

    public int updatePasswordByEmail(@Param("email") String email,
                                     @Param("password") String password);

    public void updateUserProfileByEmail(@Param("nickname") String nickname,
                                         @Param("imageUrl") String imageUrl,
                                         @Param("gender") String gender,
                                         @Param("height") Double height,
                                         @Param("weight") Double weight,
                                         @Param("age") Integer age,
                                         @Param("goal") String goal,
                                         @Param("email") String email);

}