package org.buddyguard.bodyguard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.buddyguard.bodyguard.entity.Comment;
import org.buddyguard.bodyguard.vo.CommentWithWriter;

import java.util.List;

@Mapper
public interface CommentRepository {

    public void create(Comment comment);

    public List<CommentWithWriter> findByPostId(int postId);

    public List<CommentWithWriter> findByCommentWithWriter(int postId);

    public int countByPostId(int postId);

    public Comment findById(int id);

    public void deleteById(int id);

    public void deleteByPostId(int postId);


}