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
    private final CategoryService categoryService;

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
