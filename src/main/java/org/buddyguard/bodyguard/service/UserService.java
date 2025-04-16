package org.buddyguard.bodyguard.service;

import lombok.RequiredArgsConstructor;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updateUserProfile(int id, ProfileUpdateRequest request) {
        userRepository.updateUserProfileById(
                request.getNickname(),
                request.getImageUrl(),
                request.getGender(),
                request.getHeight(),
                request.getWeight(),
                request.getAge(),
                request.getGoal(),
                id
        );
    }
}