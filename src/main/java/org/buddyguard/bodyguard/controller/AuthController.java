package org.buddyguard.bodyguard.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.entity.Verification;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.repository.VerificationRepository;
import org.buddyguard.bodyguard.request.FindPasswordRequest;
import org.buddyguard.bodyguard.request.LoginRequest;
import org.buddyguard.bodyguard.service.KakaoApiService;
import org.buddyguard.bodyguard.service.MailService;
import org.buddyguard.bodyguard.service.NaverApiService;
import org.buddyguard.bodyguard.vo.KakaoTokenResponse;
import org.buddyguard.bodyguard.vo.NaverProfileResponse;
import org.buddyguard.bodyguard.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

    private KakaoApiService kakaoApiService;
    private NaverApiService naverApiService;
    private UserRepository userRepository;
    private MailService mailService;
    private VerificationRepository verificationRepository;



    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupGetHandle(Model model) {

        return "auth/signup";
    }


    // 회원가입 처리
    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user,
                                   HttpSession session) {

        User found = userRepository.findByEmail(user.getEmail());

        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");

            userRepository.save(user);

            // user가 null인 경우 방어
            User savedUser = userRepository.findByEmail(user.getEmail());
            mailService.sendWelcomeHtmlMessage(savedUser); // 환영 메일 발송

            session.setAttribute("user", savedUser);


        }
        return "/auth/index";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginHandel(Model model) {

        model.addAttribute("kakaoClientId", "bf5fe161a4dc3d2d9956d3e8ac2ed43c");
        model.addAttribute("kakaoRedirectUri", "http://localhost:8080/auth/kakao/callback");

        model.addAttribute("naverClientId", "2SReHWgzgwqdK2WrEcNr");
        model.addAttribute("naverRedirectUri", "http://localhost:8080/auth/naver/callback");

        return "auth/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String loginPostHandle(@ModelAttribute LoginRequest loginRequest,
                                  HttpSession session,
                                  @RequestParam(value = "remember", required = false) String remember,
                                  HttpServletResponse response) {

        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            session.setAttribute("user", user);

            // 쿠키 부분
            if ("on".equals(remember)) {
                String loginKey = user.getProvider() + ":" + user.getId();
                Cookie cookie = new Cookie("autoLogin", loginKey);
                cookie.setMaxAge(60 * 60 * 24); // 하루
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            return "redirect:/index";
        }

        return "redirect:/auth/login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logoutHandle(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        Cookie cookie = new Cookie("autoLogin", null);
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setPath("/"); // 모든 경로에서 삭제
        response.addCookie(cookie);

        return "redirect:/auth/login";
    }


    // 회원 탈퇴 페이지
    @GetMapping("/leave")

    public String leaveHandle(@SessionAttribute("user") User user) {
        if (user == null) {

            return "auth/login";
        }
        return "auth/leave";
    }

    // 회원 탈퇴 처리
    @PostMapping("/leave")
    public String leavePostHandle(@SessionAttribute("user") User user, HttpSession session) {

        if (user == null) {


            return "auth/login";
        }

        verificationRepository.deleteByEmail(user.getEmail());
        userRepository.deleteByEmail(user.getEmail());
        session.invalidate();

        return "redirect:/index";
    }


    @GetMapping("/about")
    public String aboutHandle(Model model) {

        return "auth/about";
    }

    @GetMapping("/create")
    public String createHandle(Model model) {

        return "group/create";
    }

    @GetMapping("/soon")
    public String comingsoon(Model model) {

        return "auth/soon";
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
        String imageUrl = decodedJWT.getClaim("imageUrl").asString();

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

        }

        return "redirect:/";
    }


    // 네이버 소셜 로그인 처리
    @GetMapping("/naver/callback")
    public String naverCallbackHandle(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpSession session) throws JsonProcessingException {

        NaverTokenResponse tokenResponse = naverApiService.exchangeToken(code, state);
        NaverProfileResponse profileResponse = naverApiService.exchangeProfile(tokenResponse.getAccessToken());
        log.info("profileResponse id = {}", profileResponse.getId());
        log.info("profileResponse nickname = {}", profileResponse.getNickname());
        log.info("profileResponse profileImage = {}", profileResponse.getProfileImage());

        User found = userRepository.findByProviderAndProviderId("NAVER", profileResponse.getId());

        // 세션에 user 값이 없다면
        // user 객체를 만들어서 save
        if (found == null) {
            User user = User.builder()
                    .provider("NAVER")
                    .providerId(profileResponse.getId())
                    .nickname(profileResponse.getNickname())
                    .imageUrl(profileResponse.getProfileImage())
                    .verified("T")
                    .build();

            userRepository.save(user);

            session.setAttribute("user", user);


        } else {   // DB에 있는 user 라면

            session.setAttribute("user", found);
        }

        return "redirect:/index";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find-password")
    public String findPasswordHandle(Model model) {

        return "auth/find-password";
    }

    // 비밀번호 찾기 처리
    @PostMapping("/find-password")
    public String findPasswordPostHandle(@ModelAttribute @Valid FindPasswordRequest req,
                                         BindingResult result,
                                         Model model) {

        // 유효성 검사 실패시
        if (result.hasErrors()) {
            model.addAttribute("error", "이메일 형식이 아닙니다.");

            return "auth/find-password-error";
        }

        User found = userRepository.findByEmail(req.getEmail());

        // 가입된 이메일이 아니면
        if (found == null) {
            model.addAttribute("error", "해당 이메일로 임시번호를 전송할 수 없습니다.");

            return "auth/find-password-error";
        }

        // 임시 비밀번호(랜덤) 생성 후 저장 및 메일 발송
        String temporalPassword = UUID.randomUUID().toString().substring(0, 8);
        userRepository.updatePasswordByEmail(req.getEmail(), temporalPassword);
        mailService.sendTemporalPasswordMessage(req.getEmail(), temporalPassword);

        return "auth/find-password-success";
    }


    // 이메일 토큰 유효성 검사
    @GetMapping("/email-verify")
    public String emailVerifyHandle(@RequestParam("token") String token,
                                    Model model, HttpSession session) {

        Verification found = verificationRepository.findByToken(token);

        if (found == null) {
            model.addAttribute("error", "유효하지 않은 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        if (LocalDateTime.now().isAfter(found.getExpiresAt())) {
            model.addAttribute("error", "유효기간이 만료된 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        String userEmail = found.getUserEmail();
        userRepository.updateVerifiedByEmail(userEmail);

        User updatedUser = userRepository.findByEmail(userEmail);
        session.setAttribute("user", updatedUser);

        return "auth/email-verify-success";
    }

    // 인증 메일 재전송
    @GetMapping("/send-token")
    public String sendTokenHandle(@SessionAttribute("user") User user,
                                  Model model) {

        String token = UUID.randomUUID().toString().replace("-", "");

        Verification one = Verification.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .userEmail(user.getEmail())
                .build();

        verificationRepository.save(one);
        mailService.sendVerificationMessage(user, one);

        return "auth/send-token";
    }

}

