package org.buddyguard.bodyguard.controller;


import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.GroupMember;
import org.buddyguard.bodyguard.repository.GroupMemberRepository;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.buddyguard.bodyguard.entity.User;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@AllArgsConstructor
public class IndexController {

    private GroupMemberRepository groupMemberRepository;
    private UserRepository userRepository;

    // @GetMapping({"/index", "/"})
    //public String indexHandel() {

    //    return "index";
    // }

    @GetMapping({"/index", "/"})
    public String indexHandel(Model model,
                              @SessionAttribute(value = "user", required = false) User user,
                              @CookieValue(value = "autoLogin", required = false) String userCookie,
                              HttpSession session) {

        // 세션이 없고, 쿠키가 있을 경우 자동 로그인 처리
        if (user == null && userCookie != null) {
            try {
                String[] parts = userCookie.split(":"); // [provider, id]
                if (parts.length == 2) {
                    int userId = Integer.parseInt(parts[1]); // 여기 int로!
                    User found = userRepository.findById(userId); // Optional 아님!
                    if (found != null) {
                        session.setAttribute("user", found);
                        user = found;
                    }
                }
            } catch (Exception e) {
                // 잘못된 쿠키 형식이나 파싱 오류는 무시
            }
        }

        // 로그인된 유저가 있다면 groupMember 로직 실행
        if (user != null) {
            groupMemberRepository.findByUserId(user.getId());
        }

        model.addAttribute("user", user);
        return "auth/index";
    }

//    @GetMapping("/index")
//    public String indexPage(Model model, @SessionAttribute(name = "user", required = false) User user) {
//        model.addAttribute("user", user);
//        return "index";
//    }


    @GetMapping("/help")
    public String helpHandel() {

        return "help";
    }
}


