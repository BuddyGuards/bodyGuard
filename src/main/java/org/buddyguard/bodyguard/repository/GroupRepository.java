package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Group;

@Mapper
public interface GroupRepository {

    public int create (Group group);
    public int addMemberCountById (String groupId);

}
