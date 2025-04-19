package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.GroupMember;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupMemberRepository {
    public int createApproved (GroupMember groupMember);

    public GroupMember findByUserIdAndGroupId (Map params);

    public List<GroupMember> findByUserId (int userId);

    public int createPending (GroupMember groupMember);

    public int deleteById (int id);

    public int deleteByGroupId (String groupId);

    public int updateJoinedAtById (int id);

    public List<GroupMember> findPendingMembers(String groupId);

    public List<GroupMember> findApprovedMembers(String groupId);


}
