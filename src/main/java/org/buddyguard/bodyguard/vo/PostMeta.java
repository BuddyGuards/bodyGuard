package org.buddyguard.bodyguard.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.buddyguard.bodyguard.query.FeelingStats;

import java.util.List;

@Setter
@Getter
@Builder
public class PostMeta {
    private int id;
    private String content;
    private String writerName;
    private String writerAvatar;
    private String time;

    private List<FeelingStats> reactions;

}
