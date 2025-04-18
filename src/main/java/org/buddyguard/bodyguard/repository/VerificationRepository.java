package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.Verification;

@Mapper
public interface VerificationRepository {
    public int save(Verification verification);

    public Verification findByToken(@Param("token") String token);

    public int deleteByEmail(String email);

}
