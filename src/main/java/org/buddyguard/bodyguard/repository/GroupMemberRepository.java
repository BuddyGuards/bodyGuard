package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.GroupMember;

import java.util.Map;

@Mapper
public interface GroupMemberRepository {
    public int createApproved (GroupMember groupMember);

    public GroupMember findByUserIdAndGroupId (Map params);
}
