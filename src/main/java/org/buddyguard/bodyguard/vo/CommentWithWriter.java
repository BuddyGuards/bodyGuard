package org.buddyguard.bodyguard.vo;


import lombok.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentWithWriter {
    private int id;
    private int postId;
    private int writerId;
    private String content;
    private LocalDateTime wroteAt;

    private String nickname;

}
