package Remoa.BE.config;

import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Service.CategoryService;
import Remoa.BE.Member.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbInit {
    private final MemberService memberService;
    private final CategoryService categoryService;

    /**
     * @PostConstruct로 Admin 계정을 생성해주는 메서드
     */
    @PostConstruct
    public void createAdminUser() {
        if (memberService.isAdminExist()) {
            //do nothing
            log.info("============Admin is already exist============");
        } else {
            Member adminMember = new Member();
            adminMember.setEmail("spparta@gmail.com");
            adminMember.setPassword("admin");
            adminMember.setName("admin");
            adminMember.setBirth("00000000");
            adminMember.setSex(true);
            adminMember.setPhoneNumber("01000000000");
            adminMember.setOneLineIntroduction("관리자입니다.");
            adminMember.setTermConsent(true);

            adminMember.setRole("ROLE_ADMIN,ROLE_USER");
            memberService.join(adminMember);
            log.info("============Add Admin user completely============");
        }
    }

    /**
     * @PostConstruct로 Category를 세팅해주는 메서드
     */
    @PostConstruct
    public void initCategories() {
        if (!categoryService.findAllCategories().isEmpty()) {
            //do nothing
            log.info("==========Categories are already set==========");
        } else {
            Category idea = new Category("idea");
            Category marketing = new Category("marketing");
            Category design = new Category("design");
            Category video = new Category("video");
            Category etc = new Category("etc");
            this.categoryService.persistCategory(idea, marketing, design, video, etc);
            log.info("==========Setting Categories completely==========");
        }
    }

}
