package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Group;

import java.util.List;

@Mapper
public interface GroupRepository {

    public int create (Group group);
    public int addMemberCountById (String groupId);

    public List<Group> findByNameLikeOrGoalLike (String word);

    public Group findById (String id);

    public int subtractMemberCountById (String id);

}
