package org.buddyguard.bodyguard.vo;

import lombok.Data;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.Post;

@Data
public class PostWithGroup {
    private Post post;
    private Group group;
    private int commentCount;   // 댓글수
}
