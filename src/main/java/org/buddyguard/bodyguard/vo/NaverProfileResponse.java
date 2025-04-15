package org.buddyguard.bodyguard.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NaverProfileResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;
}
