package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;


@Mapper
public interface UserRepository {

    public int save(User user);

    public User findByEmail(String email);

    public void updateUserProfileByEmail(@Param("email") String email,
                                         @Param("request") ProfileUpdateRequest request);
}
