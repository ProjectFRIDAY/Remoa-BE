package Remoa.BE.Member.Service;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long join(Member member) {
      //  validateDuplicateMember(member);
//        member.hashPassword(this.bCryptPasswordEncoder);
        memberRepository.save(member);
        return member.getMemberId();

    }

    private void validateDuplicateMember(Member member) {
        log.info("member={}", member.getEmail());
        Optional<Member> findMembers = memberRepository.findByEmail(member.getEmail());
        if (findMembers.isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }


    public Boolean isNicknameDuplicate(String nickname) {
        List<Member> findMembers = memberRepository.findByNickname(nickname);
        return !(findMembers.size()==0);
    }


    public Member findOne(Long memberId) {
        Optional<Member> member = memberRepository.findOne(memberId);
        return member.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    public Optional<Member> findByKakaoId(Long kakaoId) {
        return memberRepository.findByKakaoId(kakaoId);
    }


    public Boolean isAdminExist() {
        return memberRepository.findByEmail("spparta@gmail.com").isPresent();
    }
}
