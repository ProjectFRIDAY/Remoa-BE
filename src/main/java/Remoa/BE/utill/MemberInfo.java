package Remoa.BE.utill;

import Remoa.BE.Member.Domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MemberInfo {
    public static Long getMemberId() {
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return member.getMemberId();
    }

    public static boolean authorized(HttpServletRequest request){
        SecurityContext context = SecurityContextHolder.getContext();
        HttpSession session = request.getSession(false);
        return session != null && context.getAuthentication() != null;
    }


    /**
     * Spring Security가 기본값으로 form data를 사용해 로그인을 진행하는데, Rest API를 이용해 json을 주고받는 방식으로 로그인을 처리하기 위해
     * 우회적인 방식으로 Spring Security를 이용할 수 있게 해주는 메서드.
     */
    public static void securityLoginWithoutLoginForm(Member member, HttpServletRequest request) {

        //로그인 세션에 들어갈 권한을 설정합니다.
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(member.getRole()));

        SecurityContext sc = SecurityContextHolder.getContext();

        //아이디, 패스워드, 권한을 설정합니다. 아이디는 Object단위로 넣어도 무방하며
        //패스워드는 null로 하여도 값이 생성됩니다.
        sc.setAuthentication(new UsernamePasswordAuthenticationToken(member, null, list));

    }
}
