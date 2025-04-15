package org.buddyguard.bodyguard.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.request.LoginRequest;
import org.buddyguard.bodyguard.service.KakaoApiService;
import org.buddyguard.bodyguard.vo.KakaoTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

    private UserRepository userRepository;
    private KakaoApiService kakaoApiService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupGetHandle(Model model) {

        return "auth/signup";
    }


    // 회원가입 처리
    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user) {

        User found = userRepository.findByEmail(user.getEmail());
        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
        }
        return "redirect:/index";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginHandel(Model model) {

        model.addAttribute("kakaoClientId", "bf5fe161a4dc3d2d9956d3e8ac2ed43c");
        model.addAttribute("kakaoRedirectUri", "http://localhost:8080/auth/kakao/callback");

        return "auth/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String loginPostHandel(@ModelAttribute LoginRequest loginRequest,
                                  HttpSession session) {

        User user =
                userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            session.setAttribute("user", user);
            return "redirect:/index";
        }

        return "redirect:/auth/login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logoutHandle(HttpSession session) {

        session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping("/about")
    public String aboutHandle(Model model) {

        return "auth/about";
    }
    @GetMapping("/create")
    public String createHandle(Model model) {

        return "group/create";
    }





    // 카카오 소셜 로그인 처리
    @GetMapping("/kakao/callback")
    public String kakaoCallbackHandle(@RequestParam("code") String code,
                                      HttpSession session)
            throws JsonProcessingException {

        KakaoTokenResponse response = kakaoApiService.exchangeToken(code);

        log.info("response.idToken = {}", response.getIdToken());

        DecodedJWT decodedJWT = JWT.decode(response.getIdToken());
        String sub = decodedJWT.getClaim("sub").asString();
        String nickname = decodedJWT.getClaim("nickname").asString();
        String imageUrl = decodedJWT.getClaim("picture").asString();

        User found = userRepository.findByProviderAndProviderId("KAKAO", sub);

        if (found != null) {   // 세션에 user 값이 있다면
            session.setAttribute("user", found); // 기존 유저 로그인

        } else {   // DB에 없는 user 라면
            // 새 유저 객체 생성 후 save 및 로그인
            User user = User.builder()
                    .provider("KAKAO")
                    .providerId(sub)
                    .nickname(nickname)
                    .imageUrl(imageUrl)
                    .verified("T")
                    .build();

            userRepository.save(user);
            session.setAttribute("user", user);

            // session.setAttribute("userId", user.getId());
        }

        // log.info("decodedJWT: sub={}, nickname={}, imageUrl={}", sub, nickname, picture);

        return "redirect:/";
    }

}
