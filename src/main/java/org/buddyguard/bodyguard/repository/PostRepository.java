package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.buddyguard.bodyguard.entity.Post;

import java.util.List;

@Mapper
public interface PostRepository {

    public int create (Post post);

    public List<Post> findByGroupId (String groupId);

    public Post findById(int postId);

    // 내가 작성한 글 조회
    public List<Post> findByWriterId(int writerId);

    List<Post> findByWriterIdAndGroupId(int writerId, String groupId);

    // 게시글 삭제
    public void deleteById(int id);


    public int deleteByWriterId(@Param("writerId") int writerId);


}
