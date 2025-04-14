package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.GroupMember;

@Mapper
public interface GroupMemberRepository {
    public int createApproved (GroupMember groupMember);
}
