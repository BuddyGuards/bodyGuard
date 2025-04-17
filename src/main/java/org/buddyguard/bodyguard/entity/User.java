package org.buddyguard.bodyguard.entity;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;
    private String email;
    private String password;
    private String nickname;
    private String imageUrl;
    private String provider;
    private String providerId;
    private String verified;
    private LocalDateTime createdAt;
    private String gender;
    private Double height;
    private Double weight;
    private Integer age;
    private String goal;
    private String infoPublic;   // 프로필 공개 여부

}
