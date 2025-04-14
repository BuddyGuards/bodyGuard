package org.buddyguard.bodyguard.service;

import lombok.RequiredArgsConstructor;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void updateUserProfile(String email, ProfileUpdateRequest request) {
        userRepository.updateUserProfileByEmail(email, request);
    }
}
