package org.buddyguard.bodyguard.entity;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
public class Post {
    private Integer id;
    private String groupId;
    private Integer writerId;
    private String content;
    private LocalDateTime wroteAt;
    private String imageUrl;
}
