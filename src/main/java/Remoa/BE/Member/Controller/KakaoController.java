package Remoa.BE.Member.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Req.ReqSignupDto;
import Remoa.BE.Member.Dto.Res.ResSignupDto;
import Remoa.BE.Member.Service.KakaoService;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Member.Service.ProfileService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static Remoa.BE.exception.CustomBody.*;
import static Remoa.BE.utill.MemberInfo.*;


@RestController
@Slf4j
@RequiredArgsConstructor
public class KakaoController {



    private final KakaoService ks;
    private final MemberService memberService;
    private final ProfileService profileService;

    /**
     * 카카오 로그인을 통해 code를 query string으로 받아오면, 코드를 통해 토큰, 토큰을 통해 사용자 정보를 얻어와 db에 해당 사용자가 존재하는지 여부를
     * 파악해 존재할 때는 로그인, 없을 땐 회원가입 페이지로 넘어가게 해줌.
     */
    @GetMapping("/login/kakao")
    public ResponseEntity<Object> getCI(@RequestParam String code, HttpServletRequest request) throws IOException {
        log.info("code = " + code);

        // 액세스 토큰과 유저정보 받기
        String access_token = ks.getToken(code);
        Map<String, Object> userInfo = ks.getUserInfo(access_token);

        log.info("userInfo = {}", userInfo.values());

        Long kakaoId = Long.parseLong((String) userInfo.get("id"));
        Optional<Member> member = memberService.findByKakaoId(kakaoId);

        log.info("kakaoId = {}", kakaoId);

        /*
         * 백에서 보낼 게 없을 때에도 정상 처리됐다는 메세지 정도는 같이 보내주는 게 좋습니다.
         * 상태메세지는 body에, 상태코드는 head에 들어갑니다.
         * 에러메세지를 exception 패키지처럼 한곳에 모아놓고 쓰는 것도 좋습니다.
         */
        if (member.isPresent()) {
            securityLoginWithoutLoginForm(member.get(), request);
            //if문에 걸리지 않았다면 이미 회원가입이 진행돼 db에 kakaoId가 있는 유저이므로 kakaoMember가 존재하므로 LoginController처럼 로그인 처리 하면 됩니다.
            return successResponse(CustomMessage.OK, member.get().getNickname());

        } else {
            //kakaoId가 db에 없으므로 kakaoMember가 null이므로 회원가입하지 않은 회원. 따라서 회원가입이 필요하므로 회원가입하는 uri로 redirect 시켜주어야 함.
            return successResponse(CustomMessage.OK_SIGNUP, userInfo);
        }
    }

    /**
     * front-end에서 회원가입에 필요한 정보를 넘겨주면 KakaoSignupForm으로 받아 회원가입을 진행시켜줌
     */
    @PostMapping("/signup/kakao")
    public ResponseEntity<Object> signupKakaoMember(@RequestBody @Validated ReqSignupDto form, HttpServletRequest request) throws IOException {

        Member member = new Member();
        Random random = new Random();

        //닉네임 사용 가능하면 그대로 진행, 불가능하면 임의 닉네임 "유저-{randomInt}로 지정.
        String randomNumber = Integer.toString((random.nextInt(900_000) + 100_000)); // 100_000 ~ 999_999
        boolean nicknameDuplicate = memberService.isNicknameDuplicate("유저-" + randomNumber);
        while (nicknameDuplicate) { //특수문자는 닉네임에 사용할 수 없으나 임의로 지정하는 닉네임에는 사용 가능하게 해서 또 다른 중복 문제 없게끔.
            randomNumber = Integer.toString((random.nextInt(900_000) + 100_000)); // 100_000 ~ 999_999
            nicknameDuplicate = memberService.isNicknameDuplicate("유저-" + randomNumber);
        }
        member.setNickname("유저-" + randomNumber);


        //카카오에서 받은 프로필 사진 url 링크를 토대로 s3에 저장
        if (memberService.findByKakaoId(form.getKakaoId()).isPresent()) {
            return failResponse(CustomMessage.VALIDATED, "kakaoId가 이미 가입되어 있습니다.");
        }
        member.setKakaoId(form.getKakaoId());
        member.setEmail(form.getEmail());
        member.setTermConsent(form.getTermConsent());

        Long kakaoId = form.getKakaoId();
        if(Objects.equals(kakaoId,2649301685L)){
            member.setRole("ROLE_ADMIN");
        }
        else{
            member.setRole("ROLE_USER");
        }
        memberService.join(member);
        securityLoginWithoutLoginForm(member, request);

        ResSignupDto result =  ResSignupDto.builder().
                kakaoId(member.getKakaoId()).
                email(member.getEmail()).
                nickname(member.getNickname()).
                profileImage(member.getProfileImage()).
                termConsent(member.getTermConsent()).
                build();

        return successResponse(CustomMessage.OK,result);
    }

    /**
     * 로그아웃 기능
     세션무효화, jsession쿠키를 제거,
     */
    @PostMapping ("/user/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request){
        if(authorized(request)) {

            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

            return new ResponseEntity<>(HttpStatus.OK);

        }

        return errorResponse(CustomMessage.UNAUTHORIZED);
    }


}