package org.buddyguard.bodyguard.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.request.ProfileUpdateRequest;
import org.buddyguard.bodyguard.service.UserService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/my")
@AllArgsConstructor
public class MyController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 마이페이지 화면 핸들러
    @GetMapping("/profile")
    public String profileHandle(Model model, @SessionAttribute("user") @Nullable User user) {

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        return "my/profile";
    }

    // 마이페이지 업데이트 핸들러
    @GetMapping("/profile-update")
    public String profileUpdateHandle(Model model, @SessionAttribute("user") @Nullable User user) {

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);

        return "my/profile-update";
    }

    // 마이페이지 업데이트 처리 핸들러
    @PostMapping("/profile-update")
    public String updateProfile(@SessionAttribute("user") User user, HttpSession session,
                                @ModelAttribute ProfileUpdateRequest profileUpdateRequest) {

        userService.updateUserProfile(user.getEmail(), profileUpdateRequest);

        User updatedUser = userRepository.findByEmail(user.getEmail());

        session.setAttribute("user", updatedUser);

        return "redirect:/my/profile";
    }
}
