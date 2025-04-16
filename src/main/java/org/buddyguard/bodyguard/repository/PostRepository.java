package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Post;

import java.util.List;

@Mapper
public interface PostRepository {

    public int create (Post post);

    public List<Post> findByGroupId (String groupId);
}
