package Remoa.BE.Member.Service;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void join(Member member) {
        validateDuplicateMember(member);
        member.setRole("USER");
        memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) {
        log.info("member={}", member.getEmail());
        List<Member> findMembers = memberRepository.findByEmail(member.getEmail());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public Boolean isNicknameDuplicate(Member member) {
        log.info("member={}", member.getNickname());
        List<Member> findMembers = memberRepository.findByNickname(member.getNickname());
        if (!findMembers.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }


    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    public Boolean isAdminExist() {
        return !memberRepository.findByEmail("spparta@gmail.com").isEmpty();
    }
}