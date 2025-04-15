package org.buddyguard.bodyguard.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class Post {
    private int id;
    private String groupId;
    private int writerId;
    private String content;
    private LocalDateTime wroteAt;
}
