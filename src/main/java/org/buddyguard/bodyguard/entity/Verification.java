package org.buddyguard.bodyguard.entity;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verification {
    private int id;
    private String token;
    private String userEmail;
    private LocalDateTime expiresAt;
}
