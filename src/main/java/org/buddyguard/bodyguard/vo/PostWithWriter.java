package org.buddyguard.bodyguard.vo;

import lombok.*;
import org.buddyguard.bodyguard.entity.Comment;
import org.buddyguard.bodyguard.entity.Post;
import org.buddyguard.bodyguard.entity.User;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostWithWriter {
    private Post post;
    private User writer;
    private List<CommentWithWriter> comments;

    private Map<String, Integer> reactions;

}
