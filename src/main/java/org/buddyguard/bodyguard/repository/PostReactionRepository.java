package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.PostReaction;
import org.buddyguard.bodyguard.query.FeelingStats;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostReactionRepository {

    public int create(PostReaction postReaction);

    public List<PostReaction> findByPostId(int postId);

    public PostReaction findByWriterIdAndPostId(Map<String, Object> map);

    public int deleteById(int id);

    public List<FeelingStats> countFeelingByPostId(@Param("postId") int postId);

    
}
