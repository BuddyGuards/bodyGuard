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

    private UserRepository userRepository;
    private UserService userService;

    // 마이페이지 화면
    @GetMapping("/profile")
    public String profileHandle(@SessionAttribute("user") @Nullable User user,
                                Model model) {

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);

        return "my/profile";
    }


    // 마이페이지 업데이트
    @GetMapping("/profile-update")
    public String profileUpdateHandle(Model model, @SessionAttribute("user") @Nullable User user) {

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);

        return "my/profile-update";
    }

    // 마이페이지 업데이트 처리
    @PostMapping("/profile-update")
    public String updateProfile(@SessionAttribute("user") User user, HttpSession session,
                                @ModelAttribute ProfileUpdateRequest profileUpdateRequest) {

        // 프로필 업데이트 요청 처리
        userService.updateUserProfile(user.getId(), profileUpdateRequest);

        //  DB에서 업데이트된 사용자 정보 조회
        User updatedUser = userRepository.findById(user.getId());

        // 세션에 업데이트된 사용자 정보 저장
        session.setAttribute("user", updatedUser);

        return "redirect:/my/profile";
    }
}
