package org.buddyguard.bodyguard.entity;

import jdk.jshell.Snippet;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Comment {
    private int id;
    private int postId;
    private int writerId;
    private String content;
    private LocalDateTime wroteAt;

    private String nickname;

}
