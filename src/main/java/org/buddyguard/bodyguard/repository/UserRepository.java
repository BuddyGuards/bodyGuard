package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.User;


@Mapper
public interface UserRepository {

    public int save (User user);
    public User findByEmail(String email);

}
