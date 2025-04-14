package org.buddyguard.bodyguard.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.request.LoginRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

    private UserRepository userRepository;


    @GetMapping("/signup")
    public String signupGetHandle(Model model){

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user){

        User found = userRepository.findByEmail(user.getEmail());
        if(found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
        }
        return "redirect:/index";
    }

    @GetMapping("/login")
    public String loginHandel(Model model){


        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPostHandel(@ModelAttribute LoginRequest loginRequest,
                                  HttpSession session){

        User user =
                userRepository.findByEmail(loginRequest.getEmail());
        if(user != null && user.getPassword().equals(loginRequest.getPassword())){
            session.setAttribute("user", user);
            return "redirect:/index";
        }

        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logoutHandle(HttpSession session){
        // session.removeAttribute("user");
        session.invalidate();
        return "redirect:/auth/login";
    }
    @GetMapping("/home")
    public String homeHandel(Model model){


        return "auth/home";
    }
    @GetMapping("/about")
    public String aboutHandle(Model model) {
        return "auth/about";
    }

}
