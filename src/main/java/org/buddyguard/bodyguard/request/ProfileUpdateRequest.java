package org.buddyguard.bodyguard.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String nickname;
    private String imageUrl;
    private String gender;
    private double height;
    private double weight;
    private int age;
    private String goal;
    private String infoPublic;   // 프로필 공개 여부
}