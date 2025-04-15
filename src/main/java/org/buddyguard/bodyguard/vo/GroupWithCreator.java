package org.buddyguard.bodyguard.vo;

import lombok.*;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.User;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupWithCreator {
    private Group group;
    private User creator;
}
