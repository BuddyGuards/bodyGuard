package org.buddyguard.bodyguard.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.vo.NaverTokenResponse;
import org.buddyguard.bodyguard.vo.NaverProfileResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class NaverApiService {
    private ObjectMapper objectMapper;

    public NaverTokenResponse exchangeToken(String code, String state) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "2SReHWgzgwqdK2WrEcNr");
        body.add("client_secret", "b8yFjxv9au");
        body.add("code", code);
        body.add("state", "BuddyGuard");

        ResponseEntity<String> response = restTemplate.exchange("https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        return objectMapper.readValue(response.getBody(), NaverTokenResponse.class);
    }

    public NaverProfileResponse exchangeProfile(String accessToken) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + accessToken);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                new HttpEntity<>(body, headers),
                String.class
        );

        String extractJson = objectMapper.readTree(response.getBody()).path("response").toString();

        return objectMapper.readValue(extractJson, NaverProfileResponse.class);
    }
}
