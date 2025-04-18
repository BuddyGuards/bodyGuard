package org.buddyguard.bodyguard.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostReaction {
    private int id;
    private String groupId;
    private int postId;
    private String feeling;
    private int writerId;
}
