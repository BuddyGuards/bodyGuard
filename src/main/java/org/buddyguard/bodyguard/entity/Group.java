package org.buddyguard.bodyguard.entity;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class Group {
    private String id;
    private String name;
    private String type;
    private String goal;
    private int creatorId;
    private LocalDateTime createdAt;
    private int memberCount;

}
