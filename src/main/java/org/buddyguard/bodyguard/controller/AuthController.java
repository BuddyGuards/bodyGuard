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
import org.buddyguard.bodyguard.repository.*;
import org.buddyguard.bodyguard.request.FindPasswordRequest;
import org.buddyguard.bodyguard.request.LoginRequest;
import org.buddyguard.bodyguard.service.KakaoApiService;
import org.buddyguard.bodyguard.service.MailService;
import org.buddyguard.bodyguard.service.NaverApiService;
import org.buddyguard.bodyguard.vo.KakaoTokenResponse;
import org.buddyguard.bodyguard.vo.NaverProfileResponse;
import org.buddyguard.bodyguard.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    // 의존성 주입
    private KakaoApiService kakaoApiService;
    private NaverApiService naverApiService;
    private UserRepository userRepository;
    private MailService mailService;
    private VerificationRepository verificationRepository;
    private CommentRepository commentRepository;
    private PostReactionRepository postReactionRepository;
    private GroupMemberRepository groupMemberRepository;

    private FoodLogRepository foodLogRepository;
    private PostRepository postRepository;
    private GroupRepository groupRepository;


    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupGetHandle(Model model) {

        return "auth/signup";
    }


    // 회원가입 처리
    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user,
                                   HttpSession session) {

        // 유저 중 이메일 중복 확인
        User found = userRepository.findByEmail(user.getEmail());

        if (found == null) {             // 중복된 이메일이 없다면
            user.setProvider("LOCAL");   // 로컬 가입자
            user.setVerified("F");       // 이메일 인증 전 상태
            userRepository.save(user);   // DB에 유저 저장

            // 저장된 유저 조회
            User savedUser = userRepository.findByEmail(user.getEmail());
            // 가입 환영 메일 발송
            mailService.sendWelcomeHtmlMessage(savedUser);
            // 세션에 유저 저장
            session.setAttribute("user", savedUser);

        }

        return "auth/index";
    }


    // 로그인 페이지
    @GetMapping("/login")
    public String loginHandel(Model model) {

        // 소셜 로그인용 clientId 및 redirect Url 전달
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

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(loginRequest.getEmail());

        // 비밀번호가 일치하면
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            // 세션에 유저 저장
            session.setAttribute("user", user);

            // 자동 로그인 체크 되어 있으면 쿠키 생성
            if ("on".equals(remember)) {
                String loginKey = user.getProvider() + ":" + user.getId();
                Cookie cookie = new Cookie("autoLogin", loginKey);
                cookie.setMaxAge(60 * 60 * 24);   // 하루동안 쿠키 유지
                cookie.setPath("/");              // 모든 경로에 적용
                response.addCookie(cookie);       // 클라이언트에 쿠기 전송
            }

            return "redirect:/index";
        }

        return "redirect:/auth/login";
    }


    // 로그아웃 처리
    @GetMapping("/logout")
    public String logoutHandle(HttpSession session, HttpServletResponse response) {

        // 세션 초기화
        session.invalidate();

        // 자동 로그인 쿠키 제거
        Cookie cookie = new Cookie("autoLogin", null);
        cookie.setMaxAge(0);   // 만료 시간 0
        cookie.setPath("/");   // 모든 경로에서 삭제
        response.addCookie(cookie);

        return "redirect:/auth/login";
    }


    // 회원 탈퇴 페이지
    @GetMapping("/leave")
    public String leaveHandle(@SessionAttribute("user") User user) {

        // 로그인 상태가 아니면 로그인 페이지로
        if (user == null) {
            return "auth/login";
        }

        return "auth/leave";
    }


    // 회원 탈퇴 처리
    @Transactional
    @PostMapping("/leave")
    public String leavePostHandle(@SessionAttribute("user") User user, HttpSession session) {

        if (user == null) {
            return "auth/login";
        }

        int userId = user.getId();
        String userEmail = user.getEmail();

        // 댓글 삭제
        commentRepository.deleteByWriterId(userId);
        // 감정 삭제
        postReactionRepository.deleteByWriterId(userId);
        // 모임 멤버 삭제
        groupMemberRepository.deleteByUserId(userId);

        // 푸드 다이어리 삭제
        foodLogRepository.deleteByUserId(userId);

        // 게시글 삭제
        postRepository.deleteByWriterId(userId);

        // 모임 삭제
        groupRepository.deleteByCreatorId(userId);


        // 인증 정보 삭제
        verificationRepository.deleteByEmail(userEmail);
        // 사용자 삭제
        userRepository.deleteByEmail(userEmail);



        // 세션 초기화
        session.invalidate();

        return "redirect:/index";
    }


    // 소개 페이지
    @GetMapping("/about")
    public String aboutHandle(Model model) {

        return "auth/about";
    }


    // 그룹 생성 페이지
    @GetMapping("/create")
    public String createHandle(Model model) {

        return "group/create";
    }


    // Coming Soon 페이지
    @GetMapping("/soon")
    public String comingsoon(Model model) {

        return "auth/soon";
    }


    // 카카오 로그인 콜백 처리
    @GetMapping("/kakao/callback")
    public String kakaoCallbackHandle(@RequestParam("code") String code,
                                      HttpSession session) throws JsonProcessingException {

        // 인가 코드로 토큰 요청
        KakaoTokenResponse response = kakaoApiService.exchangeToken(code);

        log.info("response.idToken = {}", response.getIdToken());

        // JWT 디코딩 후 사용자 정보 추출
        DecodedJWT decodedJWT = JWT.decode(response.getIdToken());
        String sub = decodedJWT.getClaim("sub").asString();            // 고유 식별값
        String nickname = decodedJWT.getClaim("nickname").asString();  // 닉네임
        String imageUrl = decodedJWT.getClaim("imageUrl").asString();  // 프로필 이미지

        // 카카오로 로그인한 사용자 조회
        User found = userRepository.findByProviderAndProviderId("KAKAO", sub);

        // 유저 값이 있다면 기존 유저 로그인
        if (found != null) {
            session.setAttribute("user", found);

        // DB에 없는 유저라면 새 유저 객체 생성
        } else {
            User user = User.builder()
                    .provider("KAKAO")
                    .providerId(sub)
                    .nickname(nickname)
                    .imageUrl(imageUrl)
                    .verified("T")   // 이메일 인증 완료
                    .build();

            // DB에 사용자 저장
            userRepository.save(user);
            // 세션에 사용자 저장
            session.setAttribute("user", user);

        }

        return "redirect:/";
    }


    // 네이버 로그인 콜백 처리
    @GetMapping("/naver/callback")
    public String naverCallbackHandle(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpSession session) throws JsonProcessingException {

        // 네이버 토큰 요청

        NaverTokenResponse tokenResponse = naverApiService.exchangeToken(code, state);
        NaverProfileResponse profileResponse = naverApiService.exchangeProfile(tokenResponse.getAccessToken());
        log.info("profileResponse id = {}", profileResponse.getId());
        log.info("profileResponse nickname = {}", profileResponse.getNickname());
        log.info("profileResponse profileImage = {}", profileResponse.getProfileImage());

        // 네이버로 로그인한 사용자 조회
        User found = userRepository.findByProviderAndProviderId("NAVER", profileResponse.getId());

        // 사용자 정보가 없다면 새 유저 객체 생성
        if (found == null) {
            User user = User.builder()
                    .provider("NAVER")
                    .providerId(profileResponse.getId())
                    .nickname(profileResponse.getNickname())
                    .imageUrl(profileResponse.getProfileImage())
                    .verified("T")   // 이메일 인증 완료
                    .build();

            // DB에 사용자 저장
            userRepository.save(user);
            // 세션에 사용자 정보 저장
            session.setAttribute("user", user);


        } else {   // DB에 있는 유저라면 기존 유저 로그인
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
                                         BindingResult result, Model model) {

        // 유효성 검사 실패 시
        if (result.hasErrors()) {
            model.addAttribute("error", "이메일 형식이 아닙니다.");

            return "auth/find-password-error";
        }

        // 이메일로 유저 찾기
        User found = userRepository.findByEmail(req.getEmail());

        // 가입된 이메일이 아니면
        if (found == null) {
            model.addAttribute("error", "해당 이메일로 임시번호를 전송할 수 없습니다.");

            return "auth/find-password-error";
        }

        // 임시 비밀번호(랜덤) 생성
        String temporalPassword = UUID.randomUUID().toString().substring(0, 8);
        // DB에서 유저 비밀번호 업데이트
        userRepository.updatePasswordByEmail(req.getEmail(), temporalPassword);
        // 임시 비밀번호를 이메일로 전송
        mailService.sendTemporalPasswordMessage(req.getEmail(), temporalPassword);

        return "auth/find-password-success";
    }


    // 비밀번호 변경 페이지
    @GetMapping("/change-password")
    public String changePasswordHandle(@SessionAttribute("user") User user, Model model) {
        // 세션에 저장된 현재 사용자 정보 전달
        model.addAttribute("user", user);

        return "auth/change-password";
    }


    // 비밀번호 변경 처리
    @PostMapping("/change-password")
    public String changePassword(@SessionAttribute("user") User user,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session, Model model) {

        // 현재 비밀번호가 맞는지 확인
        User found = userRepository.findById(user.getId());

        // 비밀번호가 다르면
        if (!found.getPassword().equals(currentPassword)) {
            model.addAttribute("error", "현재 비밀번호가 틀렸습니다.");

            return "auth/change-password";
        }

        // 새 비밀번호가 새 비밀번호 확인과 일치한다면
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");

            return "auth/change-password";
        }

        // 새 비밀번호로 업데이트
        userRepository.updatePasswordById(user.getId(), newPassword);

        // 세션에 저장된 유저 정보 갱신
        user.setPassword(newPassword);
        session.setAttribute("user", user);



        return "auth/change-password-success";
    }


    // 이메일 인증 토큰 유효성 확인
    @GetMapping("/email-verify")
    public String emailVerifyHandle(@RequestParam("token") String token,
                                    Model model, HttpSession session) {

        // DB에서 인증 토큰을 찾아 유효성 검사
        Verification found = verificationRepository.findByToken(token);

        // 인증 토큰이 없으면
        if (found == null) {
            model.addAttribute("error", "유효하지 않은 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        // 인증 토큰 유효기간 만료 확인
        if (LocalDateTime.now().isAfter(found.getExpiresAt())) {
            model.addAttribute("error", "유효기간이 만료된 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        // 이메일 인증 완료 처리
        String userEmail = found.getUserEmail();
        userRepository.updateVerifiedByEmail(userEmail);

        // 사용자 정보 갱신 후 세션에 저장
        User updatedUser = userRepository.findByEmail(userEmail);
        // 사용자 이메일 인증 상태 업데이트
        session.setAttribute("user", updatedUser);

        return "auth/email-verify-success";
    }

    // 인증 메일 재전송
    @GetMapping("/send-token")
    public String sendTokenHandle(@SessionAttribute("user") User user,
                                  Model model) {

        // 새로운 인증 토큰 생성
        String token = UUID.randomUUID().toString().replace("-", "");

        // 인증 토큰 저장 및 메일 발송
        Verification one = Verification.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))   // 토큰 유효 기간 1일
                .userEmail(user.getEmail())
                .build();

        // 인증 토큰 DB에 저장
        verificationRepository.save(one);
        // 인증 메일 발송
        mailService.sendVerificationMessage(user, one);

        return "auth/send-token";
    }
}

