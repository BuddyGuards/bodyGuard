package org.buddyguard.bodyguard.vo;

import lombok.*;
import org.buddyguard.bodyguard.entity.Post;
import org.buddyguard.bodyguard.entity.User;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostWithWriter {
    private Post post;
    private User writer;
}
